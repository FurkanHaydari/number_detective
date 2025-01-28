package com.brainfocus.numberdetective

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.brainfocus.numberdetective.databinding.ActivityGameResultBinding
import com.brainfocus.numberdetective.ui.leaderboard.LeaderboardFragment
import com.brainfocus.numberdetective.ads.AdManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class GameResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameResultBinding
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var database: DatabaseReference
    private lateinit var adManager: AdManager
    private var score = 0
    private var correctAnswer = ""
    private var guesses = listOf<String>()
    private var attempts = 0
    private var timeInSeconds = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().reference

        // Initialize AdManager
        adManager = AdManager.getInstance(this)
        adManager.initialize()
        
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
            scoreText.text = "Skor: $score"
            
            // İstatistik bilgileri
            correctAnswerText.text = correctAnswer
            guessesText.text = guesses.joinToString(", ")
            attemptsText.text = attempts.toString()
            timeText.text = formatTime(timeInSeconds)
            
            // Buton tıklamaları
            leaderboardButton.setOnClickListener {
                showLeaderboard()
            }
            
            shareButton.setOnClickListener {
                shareScore()
            }
            
            mainMenuButton.setOnClickListener {
                finish()
            }
            
            playAgainButton.setOnClickListener {
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
        val fragment = LeaderboardFragment()
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
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
            adManager.loadBannerAd(binding.adView)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading ad: ${e.message}")
        }
    }

    private fun playGameResultSound(isWin: Boolean) {
        try {
            mediaPlayer = MediaPlayer.create(this, if (isWin) R.raw.win_sound else R.raw.lose_sound)
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener {
                it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        private const val TAG = "GameResultActivity"
    }
}
