package com.brainfocus.numberdetective

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.button.MaterialButton
import com.brainfocus.numberdetective.utils.SoundManager
import androidx.activity.OnBackPressedCallback
import android.util.Log
import android.widget.Button
import android.app.ActivityOptions

class GameResultActivity : AppCompatActivity() {
    private lateinit var resultAnimation: LottieAnimationView
    private lateinit var resultText: TextView
    private lateinit var scoreText: TextView
    private lateinit var attemptsText: TextView
    private lateinit var timeText: TextView
    private lateinit var bestScoreText: TextView
    private lateinit var shareButton: Button
    private lateinit var playAgainButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_result)
        
        initializeViews()
        setupListeners()
        displayResults()
    }

    private fun initializeViews() {
        resultAnimation = findViewById(R.id.resultAnimation)
        resultText = findViewById(R.id.resultText)
        scoreText = findViewById(R.id.scoreText)
        attemptsText = findViewById(R.id.attemptsText)
        timeText = findViewById(R.id.timeText)
        bestScoreText = findViewById(R.id.bestScoreText)
        shareButton = findViewById(R.id.shareButton)
        playAgainButton = findViewById(R.id.playAgainButton)
    }

    private fun setupListeners() {
        shareButton.setOnClickListener {
            shareScore()
        }

        playAgainButton.setOnClickListener {
            startMainActivity()
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    startMainActivity()
                }
            }
        )
    }

    private fun startMainActivity() {
        try {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            
            // Geçiş animasyonunu kaldır
            finishAfterTransition()
        } catch (e: Exception) {
            Log.e("GameResultActivity", "Error starting MainActivity", e)
            finishAffinity()
        }
    }

    private fun displayResults() {
        val isWin = intent.getBooleanExtra("is_win", false)
        val score = intent.getIntExtra("score", 0)
        
        // Animasyon ve metin güncelleme
        if (isWin) {
            resultAnimation.setAnimation(R.raw.win_animation)
            resultText.text = "Tebrikler!"
        } else {
            resultAnimation.setAnimation(R.raw.lose_animation)
            resultText.text = "Tekrar Dene!"
        }
        
        resultAnimation.playAnimation()
        scoreText.text = "Skor: $score"
        
        // Diğer sonuç bilgilerini göster
        val attempts = intent.getIntExtra("attempts", 0)
        val timeSeconds = intent.getLongExtra("time_seconds", 0)
        
        attemptsText.text = "Deneme: $attempts"
        timeText.text = "Süre: ${timeSeconds}s"
    }

    private fun shareScore() {
        val score = intent.getIntExtra("SCORE", 0)
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Number Detective oyununda $score puan kazandım!")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Skoru Paylaş"))
    }

    private fun playSound(soundResourceId: Int) {
        SoundManager.getInstance(this).playSound(soundResourceId)
    }

    override fun onPause() {
        super.onPause()
        resultAnimation.pauseAnimation()
    }

    override fun onDestroy() {
        try {
            resultAnimation.cancelAnimation()
            super.onDestroy()
        } catch (e: Exception) {
            Log.e("GameResultActivity", "Error in onDestroy", e)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startMainActivity()
    }
}
