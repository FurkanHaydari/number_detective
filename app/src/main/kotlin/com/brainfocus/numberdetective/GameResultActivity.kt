package com.brainfocus.numberdetective

import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.brainfocus.numberdetective.auth.GameSignInManager
import com.brainfocus.numberdetective.database.LeaderboardDatabase
import com.brainfocus.numberdetective.model.GameLocation
import com.brainfocus.numberdetective.ui.leaderboard.LeaderboardFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class GameResultActivity : AppCompatActivity() {
    private lateinit var leaderboardDatabase: LeaderboardDatabase
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())
    private var mediaPlayer: MediaPlayer? = null
    
    companion object {
        private const val TAG = "GameResultActivity"
        const val EXTRA_SCORE = "score"
        const val EXTRA_IS_HIGH_SCORE = "isHighScore"
        const val EXTRA_IS_WIN = "isWin"
        const val EXTRA_ATTEMPTS = "attempts"
        const val EXTRA_TIME = "time"
        const val EXTRA_CORRECT_ANSWER = "correctAnswer"
        const val EXTRA_GUESSES = "guesses"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_result)

        setupFullscreen()
        leaderboardDatabase = LeaderboardDatabase()

        val score = intent.getIntExtra(EXTRA_SCORE, 0)
        val isHighScore = intent.getBooleanExtra(EXTRA_IS_HIGH_SCORE, false)
        val isWin = intent.getBooleanExtra(EXTRA_IS_WIN, false)
        val attempts = intent.getIntExtra(EXTRA_ATTEMPTS, 0)
        val time = intent.getLongExtra(EXTRA_TIME, 0L)
        val correctAnswer = intent.getStringExtra(EXTRA_CORRECT_ANSWER) ?: ""
        val guesses = intent.getStringArrayListExtra(EXTRA_GUESSES) ?: arrayListOf()

        setupViews(score, isWin, attempts, time, correctAnswer, guesses)
        playSound(isWin)
        updateLeaderboardScore()
        initializeAds()
    }

    private fun setupViews(score: Int, isWin: Boolean, attempts: Int, time: Long, correctAnswer: String, guesses: ArrayList<String>) {
        // Find all views
        val scoreText = findViewById<TextView>(R.id.scoreText)
        val resultText = findViewById<TextView>(R.id.resultText)
        val correctAnswerText = findViewById<TextView>(R.id.correctAnswerText)
        val incorrectGuessesText = findViewById<TextView>(R.id.incorrectGuessesText)
        val motivationText = findViewById<TextView>(R.id.motivationText)
        val playAgainButton = findViewById<Button>(R.id.playAgainButton)
        val leaderboardButton = findViewById<Button>(R.id.leaderboardButton)
        val timeText = findViewById<TextView>(R.id.timeText)
        val shareButton = findViewById<Button>(R.id.shareButton)
        val backButton = findViewById<Button>(R.id.backButton)
        val attemptsText = findViewById<TextView>(R.id.attemptsText)

        // Set text values
        scoreText.text = "Skorunuz: $score"
        resultText.text = if (isWin) "Tebrikler!" else "Oyun Bitti!"
        correctAnswerText.text = "$correctAnswer"
        incorrectGuessesText.text = if (guesses.isEmpty()) "-" else guesses.joinToString(", ")
        timeText.text = formatTime(time)
        attemptsText.text = attempts.toString()

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
            shareResult(score, attempts, time, isWin, guesses)
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun shareResult(score: Int, attempts: Int, time: Long, isWin: Boolean, guesses: ArrayList<String>) {
        val emoji = if (isWin) "🎉" else "🎮"
        val message = """
            $emoji Number Detective Oyun Sonucu $emoji
            ${if (isWin) "Kazandım!" else "Oyun Bitti!"}
            Skor: $score
            Deneme: $attempts
            Süre: ${formatTime(time)}
            Tahminler: ${guesses.joinToString(", ")}
            
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
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commitAllowingStateLoss()  // Use commitAllowingStateLoss instead of commit
                
            // Show fragment container and hide other views
            val fragmentContainer = findViewById<View>(R.id.fragmentContainer)
            fragmentContainer.visibility = View.VISIBLE

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

    private fun formatTime(timeInMillis: Long): String {
        val totalSeconds = (timeInMillis / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
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

    private fun playSound(isWin: Boolean) {
        try {
            val soundResId = if (isWin) R.raw.victory else R.raw.game_over
            mediaPlayer = MediaPlayer.create(this, soundResId)
            mediaPlayer?.apply {
                setOnCompletionListener { release() }
                start()
            } ?: run {
                Log.e(TAG, "Failed to create MediaPlayer")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound: ${e.message}")
        }
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

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            findViewById<View>(R.id.fragmentContainer).visibility = View.GONE
            supportFragmentManager.popBackStack()
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
