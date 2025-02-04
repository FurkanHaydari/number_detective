package com.brainfocus.numberdetective

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.brainfocus.numberdetective.ads.AdManager
import com.brainfocus.numberdetective.databinding.ActivityGameResultBinding
import com.brainfocus.numberdetective.sound.SoundManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.games.Games
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GameResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameResultBinding
    @Inject
    lateinit var adManager: AdManager
    @Inject
    lateinit var soundManager: SoundManager
    private var score = 0
    private var correctAnswer = ""
    private var guesses = listOf<String>()
    private var attempts = 0
    private var timeInSeconds = 0L

    private lateinit var leaderboardLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupFullscreen()
        setupLeaderboardLauncher()

        // Initialize sound manager
        soundManager.initialize()

        // Initialize AdManager
        adManager.initialize()
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
        
        // Get game result data from intent
        score = intent.getIntExtra("score", 0)
        correctAnswer = intent.getStringExtra("correctAnswer") ?: ""
        guesses = intent.getStringArrayListExtra("guesses") ?: listOf()
        attempts = intent.getIntExtra("attempts", 0)
        timeInSeconds = intent.getLongExtra("timeInSeconds", 0)
        
        // Setup views and listeners
        setupViews()
        setupBackPressedCallback()
        
        // Play sound effect
        playGameResultSound(true)
    }

    private fun setupLeaderboardLauncher() {
        leaderboardLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Handle result if needed
        }
    }

    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun setupViews() {
        binding.apply {
            // Skor ve başlık bilgileri
            scoreText.text = getString(R.string.score_text, score)
            
            // Win durumu kontrolü
            if (score >= 2000) {
                gameOverText.text = getString(R.string.win_text)
                gameOverText.setTextColor(getColor(android.R.color.white))
                encouragementText.text = getString(R.string.win_motivation)
            } else {
                gameOverText.text = getString(R.string.game_over_text)
                gameOverText.setTextColor(getColor(android.R.color.white))
                encouragementText.text = getString(R.string.lose_motivation)
            }
            
            // İstatistik bilgileri
            correctAnswerText.text = correctAnswer
            attemptsText.text = attempts.toString()
            
            // Tahminleri alt alta göster ve numaralandır
            val guessesString = buildString {
                guesses.forEachIndexed { index, guess ->
                    if (index > 0) append("\n")
                    append("${index + 1}. $guess")
                }
            }
            guessesText.text = guessesString
            
            // Süre bilgisi
            timeText.text = getString(R.string.time_text, timeInSeconds)
            
            // Buton tıklamaları
            leaderboardButton.setOnClickListener {
                soundManager.playButtonClick()
                showLeaderboard()
            }
            
            shareButton.setOnClickListener {
                soundManager.playButtonClick()
                shareScore()
            }
            
            mainMenuButton.setOnClickListener {
                soundManager.playButtonClick()
                finish()
            }
            
            playAgainButton.setOnClickListener {
                soundManager.playButtonClick()
                startNewGame()
            }
            
            // Reklam yükleme
            loadAd()
        }
    }
    
    private fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    private fun showLeaderboard() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            Games.getLeaderboardsClient(this, account)
                .getLeaderboardIntent(getString(R.string.leaderboard_id))
                .addOnSuccessListener { intent ->
                    leaderboardLauncher.launch(intent)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error showing leaderboard", e)
                }
        }
    }

    private fun shareScore() {
        val shareText = "Number Detective oyununda $score puan aldım! Sen de dene!"

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)))
    }

    private fun startNewGame() {
        startActivity(Intent(this@GameResultActivity, GameActivity::class.java))
        finish()
    }

    private fun loadAd() {
        try {
            binding.adView.visibility = View.VISIBLE
        } catch (e: Exception) {
            Log.e(TAG, "Error loading ad: ${e.message}")
        }
    }

    private fun playGameResultSound(isWin: Boolean) {
        try {
            if (isWin) {
                soundManager.playWinSound()
            } else {
                soundManager.playLoseSound()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound: ${e.message}", e)
        }
    }

    private fun setupFullscreen() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    override fun onResume() {
        super.onResume()
        adManager.onResume()
    }

    override fun onPause() {
        super.onPause()
        adManager.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        adManager.release()
    }

    companion object {
        private const val TAG = "GameResultActivity"
    }
}
