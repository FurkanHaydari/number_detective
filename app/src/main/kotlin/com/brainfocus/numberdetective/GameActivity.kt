package com.brainfocus.numberdetective

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.adapter.GuessHistoryAdapter
import com.brainfocus.numberdetective.base.BaseActivity
import com.brainfocus.numberdetective.utils.SoundManager
import com.brainfocus.numberdetective.utils.SoundType
import com.brainfocus.numberdetective.viewmodel.GameViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class GameActivity : BaseActivity() {
    override val viewModel: GameViewModel by viewModel()
    
    private lateinit var digit1: EditText
    private lateinit var digit2: EditText
    private lateinit var digit3: EditText
    private lateinit var guessButton: Button
    private lateinit var scoreText: TextView
    private lateinit var attemptsText: TextView
    private lateinit var guessHistoryRecyclerView: RecyclerView
    private lateinit var guessHistoryAdapter: GuessHistoryAdapter
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        
        initializeViews()
        setupRecyclerView()
        setupListeners()
        observeGameState()
    }

    private fun initializeViews() {
        digit1 = findViewById(R.id.digit1)
        digit2 = findViewById(R.id.digit2)
        digit3 = findViewById(R.id.digit3)
        guessButton = findViewById(R.id.guessButton)
        scoreText = findViewById(R.id.scoreText)
        attemptsText = findViewById(R.id.attemptsText)
        guessHistoryRecyclerView = findViewById(R.id.guessHistoryRecyclerView)
        soundManager = SoundManager.getInstance(this)
    }

    private fun setupRecyclerView() {
        guessHistoryAdapter = GuessHistoryAdapter()
        guessHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@GameActivity)
            adapter = guessHistoryAdapter
        }
    }

    private fun setupListeners() {
        guessButton.setOnClickListener {
            val guess = "${digit1.text}${digit2.text}${digit3.text}"
            if (guess.length == 3) {
                soundManager.playSound(SoundType.BUTTON_CLICK)
                viewModel.makeGuess(guess)
                clearInputs()
            } else {
                showMessage("Lütfen 3 basamaklı bir sayı girin")
            }
        }
    }

    private fun observeGameState() {
        collectFlow(viewModel.gameState) { state ->
            when (state) {
                is GameViewModel.GameState.Playing -> {
                    guessButton.isEnabled = true
                }
                is GameViewModel.GameState.Won -> {
                    handleGameWon(state.score)
                }
                is GameViewModel.GameState.Lost -> {
                    handleGameLost(state.secretNumber)
                }
                else -> {}
            }
        }

        collectFlow(viewModel.score) { score ->
            scoreText.text = "Skor: $score"
        }

        collectFlow(viewModel.attempts) { attempts ->
            attemptsText.text = "Kalan Hak: $attempts"
        }

        collectFlow(viewModel.guessHistory) { history ->
            guessHistoryAdapter.submitList(history)
        }
    }

    private fun handleGameWon(score: Int) {
        soundManager.playSound(SoundType.WIN)
        guessButton.isEnabled = false
        showGameResult(true, score)
    }

    private fun handleGameLost(secretNumber: String) {
        soundManager.playSound(SoundType.LOSE)
        guessButton.isEnabled = false
        showGameResult(false, 0, secretNumber)
    }

    private fun showGameResult(isWin: Boolean, score: Int, secretNumber: String = "") {
        GameResultActivity.start(this, isWin, score, secretNumber)
        finish()
    }

    private fun clearInputs() {
        digit1.text.clear()
        digit2.text.clear()
        digit3.text.clear()
        digit1.requestFocus()
    }
}
