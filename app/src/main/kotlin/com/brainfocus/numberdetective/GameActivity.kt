package com.brainfocus.numberdetective

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.adapter.GuessHistoryAdapter
import com.brainfocus.numberdetective.game.NumberDetectiveGame
import com.brainfocus.numberdetective.utils.AdManager
import com.brainfocus.numberdetective.utils.SoundManager
import com.brainfocus.numberdetective.utils.SoundType
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar

class GameActivity : AppCompatActivity() {
    private lateinit var game: NumberDetectiveGame
    private lateinit var digit1: EditText
    private lateinit var digit2: EditText
    private lateinit var digit3: EditText
    private lateinit var scoreText: TextView
    private lateinit var attemptsText: TextView
    private lateinit var guessButton: MaterialButton
    private lateinit var guessHistoryAdapter: GuessHistoryAdapter
    private lateinit var soundManager: SoundManager
    private lateinit var adManager: AdManager
    private var gameStartTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        soundManager = SoundManager.getInstance(this)
        adManager = AdManager.getInstance(this)
        
        initializeViews()
        setupRecyclerView()
        startNewGame()
        
        // Reklam yüklemeyi başlat
        adManager.loadInterstitialAd()
    }

    private fun initializeViews() {
        digit1 = findViewById(R.id.digit1)
        digit2 = findViewById(R.id.digit2)
        digit3 = findViewById(R.id.digit3)
        scoreText = findViewById(R.id.scoreText)
        attemptsText = findViewById(R.id.attemptsText)
        guessButton = findViewById(R.id.guessButton)

        guessButton.setOnClickListener { 
            soundManager.playSound(SoundType.BUTTON_CLICK)
            makeGuess() 
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.guessHistoryRecyclerView)
        guessHistoryAdapter = GuessHistoryAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@GameActivity)
            adapter = guessHistoryAdapter
        }
    }

    private fun startNewGame() {
        game = NumberDetectiveGame()
        game.startNewGame()
        gameStartTime = System.currentTimeMillis()
        guessHistoryAdapter.clear()
        updateUI()
        guessButton.isEnabled = true
    }

    private fun makeGuess() {
        val guess = "${digit1.text}${digit2.text}${digit3.text}"
        if (guess.length != 3 || !guess.all { it.isDigit() }) {
            showMessage("Lütfen 3 basamaklı bir sayı girin")
            return
        }

        try {
            val result = game.makeGuess(guess)
            guessHistoryAdapter.addGuess(guess, result)
            updateUI()

            when {
                game.isGameWon(guess) -> {
                    soundManager.playSound(SoundType.WIN)
                    showGameResult(true)
                }
                game.isGameOver() -> {
                    soundManager.playSound(SoundType.LOSE)
                    showGameResult(false)
                }
                else -> {
                    showMessage("Doğru yer: ${result.correct}, Yanlış yer: ${result.misplaced}")
                }
            }
        } catch (e: IllegalStateException) {
            showMessage(e.message ?: "Bir hata oluştu")
        }
    }

    private fun showGameResult(isWin: Boolean) {
        guessButton.isEnabled = false
        val gameTimeSeconds = (System.currentTimeMillis() - gameStartTime) / 1000

        // Önce reklamı göster, sonra sonuç ekranına git
        adManager.showInterstitialAd {
            val intent = Intent(this, GameResultActivity::class.java).apply {
                putExtra("IS_WIN", isWin)
                putExtra("SCORE", game.getCurrentScore())
                putExtra("ATTEMPTS", 3 - game.getRemainingAttempts())
                putExtra("TIME_SECONDS", gameTimeSeconds)
                putExtra("BEST_SCORE", 0) // TODO: Implement best score tracking
            }
            startActivity(intent)
            finish()
        }
    }

    private fun updateUI() {
        scoreText.text = "Skor: ${game.getCurrentScore()}"
        attemptsText.text = "Kalan Hak: ${game.getRemainingAttempts()}"
    }

    private fun showMessage(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
