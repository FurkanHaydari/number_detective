package com.brainfocus.numberdetective

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.brainfocus.numberdetective.viewmodel.GameState
import com.brainfocus.numberdetective.viewmodel.GameViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class GameActivity : AppCompatActivity() {
    private val viewModel: GameViewModel by viewModel()
    private lateinit var guessInput: EditText
    private lateinit var submitButton: Button
    private lateinit var newGameButton: Button
    private lateinit var hintText: TextView
    private lateinit var attemptsText: TextView
    private lateinit var scoreText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        initViews()
        setupListeners()
        observeGameState()
    }

    private fun initViews() {
        guessInput = findViewById(R.id.guessInput)
        submitButton = findViewById(R.id.submitButton)
        newGameButton = findViewById(R.id.newGameButton)
        hintText = findViewById(R.id.hintText)
        attemptsText = findViewById(R.id.attemptsText)
        scoreText = findViewById(R.id.scoreText)
    }

    private fun setupListeners() {
        submitButton.setOnClickListener {
            val guess = guessInput.text.toString()
            if (guess.isNotEmpty()) {
                viewModel.makeGuess(guess)
                guessInput.text.clear()
            }
        }

        newGameButton.setOnClickListener {
            viewModel.startNewGame()
            guessInput.text.clear()
            submitButton.isEnabled = true
        }
    }

    private fun observeGameState() {
        lifecycleScope.launch {
            viewModel.gameState.collectLatest { state ->
                when (state) {
                    is GameState.Initial -> {
                        hintText.text = "Welcome! Make your first guess!"
                        attemptsText.text = "Attempts: 0/10"
                        scoreText.text = "Score: 0"
                    }
                    is GameState.Playing -> {
                        hintText.text = state.hint
                        attemptsText.text = "Attempts: ${state.attempts}/${state.maxAttempts}"
                        scoreText.text = "Score: ${state.score}"
                        submitButton.isEnabled = true
                    }
                    is GameState.Won -> {
                        hintText.text = "Congratulations! You won with a score of ${state.score}!"
                        submitButton.isEnabled = false
                    }
                    is GameState.Lost -> {
                        hintText.text = "Game Over! The number was ${state.targetNumber}"
                        submitButton.isEnabled = false
                    }
                }
            }
        }
    }
}
