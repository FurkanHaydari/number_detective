package com.brainfocus.numberdetective

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.brainfocus.numberdetective.auth.GameSignInManager
import com.brainfocus.numberdetective.database.LeaderboardDatabase
import com.brainfocus.numberdetective.model.GameLocation
import com.brainfocus.numberdetective.ui.leaderboard.LeaderboardFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class GameResultActivity : AppCompatActivity() {
    private lateinit var leaderboardDatabase: LeaderboardDatabase
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var database: DatabaseReference
    
    companion object {
        private const val TAG = "GameResultActivity"
        const val EXTRA_SCORE = "score"
        const val EXTRA_RESULT = "result"
        const val EXTRA_CORRECT_ANSWER = "correctAnswer"
        private const val MAX_RETRY_ATTEMPTS = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_result)

        setupFullscreen()
        leaderboardDatabase = LeaderboardDatabase()
        
        // Initialize Firebase reference
        database = FirebaseDatabase.getInstance().reference
        
        val score = intent.getIntExtra(EXTRA_SCORE, 0)
        val result = intent.getStringExtra(EXTRA_RESULT) ?: ""
        val correctAnswer = intent.getStringExtra(EXTRA_CORRECT_ANSWER) ?: ""

        setupViews(score, result == "win", correctAnswer)
        playResultSound(result)
        updateLeaderboardScore()
        initializeAds()
        
        // Save score with Firebase auth check
        FirebaseAuth.getInstance().currentUser?.let { user ->
            saveScore(score)
        } ?: run {
            Log.w(TAG, "User not authenticated, attempting anonymous sign-in")
            FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener {
                    saveScore(score)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to sign in anonymously: ${e.message}")
                    saveScoreLocally(score)
                }
        }
    }

    private fun setupViews(score: Int, isWin: Boolean, correctAnswer: String) {
        // Find all views
        val scoreText = findViewById<TextView>(R.id.scoreText)
        val resultText = findViewById<TextView>(R.id.resultText)
        val correctAnswerText = findViewById<TextView>(R.id.correctAnswerText)
        val motivationText = findViewById<TextView>(R.id.motivationText)
        val playAgainButton = findViewById<Button>(R.id.playAgainButton)
        val leaderboardButton = findViewById<Button>(R.id.leaderboardButton)
        val shareButton = findViewById<Button>(R.id.shareButton)
        val backButton = findViewById<Button>(R.id.backButton)

        // Set text values
        scoreText.text = "Skorunuz: $score"
        resultText.text = if (isWin) "Tebrikler!" else "Oyun Bitti!"
        correctAnswerText.text = "$correctAnswer"

        // Set motivation text based on game result
        motivationText.text = if (isWin) {
            "Harika bir oyun! Devam et!"
        } else {
            "Daha iyisini yapabilirsin! Tekrar dene!"
        }

        // Set button click listeners
        playAgainButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        leaderboardButton.setOnClickListener {
            val account = GoogleSignIn.getLastSignedInAccount(this)
            if (account != null) {
                lifecycleScope.launch {
                    try {
                        leaderboardDatabase.updatePlayerScore(
                            userId = account.id ?: return@launch,
                            score = score,
                            location = GameLocation()
                        )
                        // Show leaderboard after score update
                        showLeaderboard()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating score: ${e.message}")
                        // Show leaderboard even if score update fails
                        showLeaderboard()
                    }
                }
            } else {
                showLeaderboard()
            }
        }

        shareButton.setOnClickListener {
            shareResult(score, isWin)
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun shareResult(score: Int, isWin: Boolean) {
        val emoji = if (isWin) "🎉" else "🎮"
        val message = """
            $emoji Number Detective Oyun Sonucu $emoji
            ${if (isWin) "Kazandım!" else "Oyun Bitti!"}
            Skor: $score
            
            Sen de oyna: https://play.google.com/store/apps/details?id=$packageName
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        startActivity(Intent.createChooser(intent, "Sonucu Paylaş"))
    }

    private fun showLeaderboard() {
        if (isFinishing || isDestroyed) {
            return
        }

        try {
            // Show leaderboard fragment
            val fragment = LeaderboardFragment()
            val fragmentContainer = findViewById<FrameLayout>(R.id.fragmentContainer)
            fragmentContainer.visibility = View.VISIBLE
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commitAllowingStateLoss()  // Use commitAllowingStateLoss instead of commit
                
            // Hide other views
            findViewById<View>(R.id.statsCard)?.visibility = View.GONE
            findViewById<View>(R.id.buttonContainer)?.visibility = View.GONE
            findViewById<View>(R.id.resultText)?.visibility = View.GONE
            findViewById<View>(R.id.scoreText)?.visibility = View.GONE
            findViewById<View>(R.id.motivationText)?.visibility = View.GONE
        } catch (e: Exception) {
            Log.e(TAG, "Error showing leaderboard: ${e.message}")
        }
    }

    private fun playResultSound(result: String) {
        val soundResId = if (result == "win") R.raw.victory else R.raw.game_over
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, soundResId)
        mediaPlayer?.start()
    }

    private fun setupFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    private fun updateLeaderboardScore() {
        val score = intent.getIntExtra(EXTRA_SCORE, 0)
        val signInManager = GameSignInManager(this)
        signInManager.initializeSignIn()
        signInManager.signIn(
            onSuccess = { account ->
                lifecycleScope.launch {
                    try {
                        leaderboardDatabase.updatePlayerScore(
                            userId = account.id ?: return@launch,
                            score = score,
                            location = GameLocation()
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating score: ${e.message}")
                    }
                }
            },
            onFailed = {
                Log.e(TAG, "Sign in failed, score not updated")
            }
        )
    }

    private fun initializeAds() {
        try {
            val adView = findViewById<com.google.android.gms.ads.AdView>(R.id.adView)
            val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
            adView.loadAd(adRequest)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading ad: ${e.message}")
        }
    }

    private fun saveScore(score: Int) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            saveScoreToDatabase(score, user.uid)
        } else {
            Log.e(TAG, "No user found, saving score locally")
            saveScoreLocally(score)
        }
    }

    private fun saveScoreToDatabase(score: Int, userId: String?, retryCount: Int = MAX_RETRY_ATTEMPTS) {
        if (userId == null) {
            Log.e(TAG, "Cannot save score: no user ID")
            saveScoreLocally(score)
            return
        }

        val location: Map<String, Any?> = mapOf(
            "country" to "Türkiye",
            "city" to (getLastKnownLocation()?.let { getCityFromLocation(it) }),
            "district" to (getLastKnownLocation()?.let { getDistrictFromLocation(it) })
        )

        val deviceInfo: Map<String, Any?> = mapOf(
            "model" to Build.MODEL,
            "manufacturer" to Build.MANUFACTURER,
            "androidVersion" to Build.VERSION.SDK_INT.toString()
        )

        val scoreData: Map<String, Any?> = mapOf(
            "score" to score,
            "timestamp" to ServerValue.TIMESTAMP,
            "location" to location,
            "deviceInfo" to deviceInfo
        )

        val scoreRef = database.child("leaderboard").child(userId)
        scoreRef.setValue(scoreData)
            .addOnSuccessListener {
                Log.d(TAG, "Score saved successfully")
                // Clear any locally saved scores that were pending upload
                clearLocalScore()
                Toast.makeText(this, "Score saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving score: ${e.message}")
                if (retryCount > 0) {
                    Log.d(TAG, "Retrying save... ($retryCount attempts remaining)")
                    // Retry with exponential backoff
                    Handler(Looper.getMainLooper()).postDelayed({
                        saveScoreToDatabase(score, userId, retryCount - 1)
                    }, (MAX_RETRY_ATTEMPTS - retryCount + 1) * 1000L)
                } else {
                    // Store score locally if all retries failed
                    saveScoreLocally(score)
                    Toast.makeText(this, "Score saved locally for later sync", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveScoreLocally(score: Int) {
        try {
            val prefs = getSharedPreferences("pending_scores", Context.MODE_PRIVATE)
            val pendingScores = prefs.getString("scores", "[]")
            val scoresArray = JSONArray(pendingScores)
            
            val location = JSONObject().apply {
                put("country", "Türkiye")
                getLastKnownLocation()?.let { loc ->
                    getCityFromLocation(loc)?.let { city ->
                        put("city", city)
                    }
                    getDistrictFromLocation(loc)?.let { district ->
                        put("district", district)
                    }
                }
            }
            
            val deviceInfo = JSONObject().apply {
                put("model", Build.MODEL)
                put("manufacturer", Build.MANUFACTURER)
                put("androidVersion", Build.VERSION.SDK_INT.toString())
            }
            
            val scoreObject = JSONObject().apply {
                put("score", score)
                put("timestamp", System.currentTimeMillis())
                put("location", location)
                put("deviceInfo", deviceInfo)
            }
            
            scoresArray.put(scoreObject)
            prefs.edit().putString("scores", scoresArray.toString()).apply()
            Log.d(TAG, "Score saved locally for later sync")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving score locally: ${e.message}")
        }
    }

    private fun getLastKnownLocation(): Location? {
        try {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
        } catch (e: Exception) {
            Log.e("Location", "Error getting location: ${e.message}")
        }
        return null
    }

    private fun getCityFromLocation(location: Location): String? {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            return addresses?.firstOrNull()?.adminArea
        } catch (e: Exception) {
            Log.e("Location", "Error getting city: ${e.message}")
        }
        return null
    }

    private fun getDistrictFromLocation(location: Location): String? {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            return addresses?.firstOrNull()?.subAdminArea
        } catch (e: Exception) {
            Log.e("Location", "Error getting district: ${e.message}")
        }
        return null
    }

    private fun clearLocalScore() {
        try {
            getSharedPreferences("pending_scores", Context.MODE_PRIVATE)
                .edit()
                .remove("scores")
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing local scores: ${e.message}")
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            val fragmentContainer = findViewById<FrameLayout>(R.id.fragmentContainer)
            fragmentContainer.visibility = View.GONE
            findViewById<View>(R.id.statsCard)?.visibility = View.VISIBLE
            findViewById<View>(R.id.buttonContainer)?.visibility = View.VISIBLE
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        mainScope.cancel()
        super.onDestroy()
    }
}
