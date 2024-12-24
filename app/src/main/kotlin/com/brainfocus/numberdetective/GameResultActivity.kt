package com.brainfocus.numberdetective

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.button.MaterialButton

class GameResultActivity : AppCompatActivity() {
    private lateinit var resultAnimation: LottieAnimationView
    private lateinit var resultText: TextView
    private lateinit var scoreText: TextView
    private lateinit var attemptsText: TextView
    private lateinit var timeText: TextView
    private lateinit var bestScoreText: TextView
    private lateinit var shareButton: MaterialButton
    private lateinit var playAgainButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_result)

        initializeViews()
        setupGameResult()
        setupButtons()
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

    private fun setupGameResult() {
        val isWin = intent.getBooleanExtra("IS_WIN", false)
        val score = intent.getIntExtra("SCORE", 0)
        val attempts = intent.getIntExtra("ATTEMPTS", 0)
        val timeSeconds = intent.getLongExtra("TIME_SECONDS", 0)
        val bestScore = intent.getIntExtra("BEST_SCORE", 0)

        // Animasyon ayarları
        resultAnimation.setAnimation(
            if (isWin) "win_animation.json" else "lose_animation.json"
        )
        resultAnimation.playAnimation()

        // Metin ayarları
        resultText.text = if (isWin) "Tebrikler!" else "Bir Dahaki Sefere!"
        scoreText.text = "Skor: $score"
        attemptsText.text = "${3 - attempts}"
        timeText.text = "${timeSeconds}s"
        bestScoreText.text = "$bestScore"

        // Ses efekti çal
        playSound(if (isWin) R.raw.win_sound else R.raw.lose_sound)
    }

    private fun setupButtons() {
        shareButton.setOnClickListener {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, createShareMessage())
            }
            startActivity(Intent.createChooser(shareIntent, "Skoru Paylaş"))
        }

        playAgainButton.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
            finish()
        }
    }

    private fun createShareMessage(): String {
        val score = intent.getIntExtra("SCORE", 0)
        return "Brain Focus'ta yeni rekor! $score puan kazandım! 🧠✨ #BrainFocus #BeyniniDinçTut"
    }

    private fun playSound(soundResourceId: Int) {
        // TODO: MediaPlayer ile ses efekti çal
    }

    override fun onBackPressed() {
        // Ana menüye dön
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
