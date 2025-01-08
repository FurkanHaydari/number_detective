package com.brainfocus.numberdetective

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.button.MaterialButton
import androidx.activity.OnBackPressedCallback
import android.util.Log
import android.widget.Button
import android.app.ActivityOptions
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.View
import android.os.Build

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
        
        // Önce setContentView çağrılmalı
        setContentView(R.layout.activity_game_result)
        
        // Sonra tam ekran ayarları yapılmalı
        setupFullscreen()
        
        initializeViews()
        setupListeners()
        displayResults()
    }

    private fun setupFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setupFullscreen()
        }
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

    override fun onPause() {
        super.onPause()
        resultAnimation.pauseAnimation()
    }

    override fun onDestroy() {
        resultAnimation.cancelAnimation()
        super.onDestroy()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startMainActivity()
    }
}
