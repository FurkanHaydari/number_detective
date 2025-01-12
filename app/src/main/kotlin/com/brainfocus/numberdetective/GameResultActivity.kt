package com.brainfocus.numberdetective

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.brainfocus.numberdetective.databinding.ActivityGameResultBinding
import com.brainfocus.numberdetective.ads.AdManager
import com.brainfocus.numberdetective.database.LeaderboardDatabase
import com.brainfocus.numberdetective.ui.leaderboard.LeaderboardFragment
import com.google.android.gms.ads.AdView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class GameResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameResultBinding
    private lateinit var leaderboardDatabase: LeaderboardDatabase
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var database: DatabaseReference
    private lateinit var adManager: AdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Database
        database = Firebase.database.reference
        
        // Initialize AdManager
        adManager = AdManager.getInstance(this)
        adManager.initialize()
        
        // Get game result data from intent
        val score = intent.getIntExtra("score", 0)
        val isWin = intent.getBooleanExtra("isWin", false)
        val correctAnswer = intent.getStringExtra("correctAnswer") ?: ""

        // Setup views and listeners
        setupViews(score, isWin, correctAnswer)
        setupBackPressedCallback()
        initializeAds()
        
        // Play sound effect based on game result
        playGameResultSound(isWin)
    }

    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                    binding.fragmentContainer.visibility = View.GONE
                    binding.statsCard.visibility = View.VISIBLE
                    binding.buttonContainer.visibility = View.VISIBLE
                } else {
                    finish()
                }
            }
        })
    }

    private fun setupViews(score: Int, isWin: Boolean, @Suppress("UNUSED_PARAMETER") correctAnswer: String) {
        binding.apply {
            scoreText.text = getString(R.string.score_text, score)
            resultText.text = if (isWin) getString(R.string.win_text) else getString(R.string.game_over_text)
            motivationText.text = if (isWin) {
                getString(R.string.win_motivation)
            } else {
                getString(R.string.lose_motivation)
            }

            playAgainButton.setOnClickListener {
                startActivity(Intent(this@GameResultActivity, GameActivity::class.java))
                finish()
            }

            backButton.setOnClickListener {
                startActivity(Intent(this@GameResultActivity, MainActivity::class.java))
                finish()
            }

            leaderboardButton.setOnClickListener {
                showLeaderboard()
            }

            shareButton.setOnClickListener {
                shareGameResult(score, isWin)
            }
        }
    }

    private fun showLeaderboard() {
        try {
            val fragment = LeaderboardFragment()
            binding.fragmentContainer.visibility = View.VISIBLE
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()

            // Hide main content
            binding.statsCard.visibility = View.GONE
            binding.buttonContainer.visibility = View.GONE
        } catch (e: Exception) {
            Log.e(TAG, "Error showing leaderboard: ${e.message}")
            Toast.makeText(this, getString(R.string.error_showing_leaderboard), Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareGameResult(score: Int, isWin: Boolean) {
        val shareText = if (isWin) {
            getString(R.string.share_win_text, score)
        } else {
            getString(R.string.share_lose_text, score)
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)))
    }

    private fun playGameResultSound(isWin: Boolean) {
        try {
            mediaPlayer = MediaPlayer.create(this, 
                if (isWin) R.raw.win_sound else R.raw.lose_sound
            )
            mediaPlayer?.start()
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound: ${e.message}")
        }
    }

    private fun initializeAds() {
        try {
            adManager.loadBannerAd(binding.adView)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading ad: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        mainScope.cancel()
    }

    companion object {
        private const val TAG = "GameResultActivity"
    }
}
