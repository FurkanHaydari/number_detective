package com.brainfocus.numberdetective

import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.widget.EditText
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.brainfocus.numberdetective.viewmodel.GameState
import com.brainfocus.numberdetective.viewmodel.GameViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class GameActivity : AppCompatActivity() {
    private val viewModel: GameViewModel by viewModel()
    private lateinit var guessInput: EditText
    private lateinit var submitButton: MaterialButton
    private lateinit var newGameButton: MaterialButton
    private lateinit var hintsContainer: TextView
    private lateinit var attemptsText: TextView
    private lateinit var scoreText: TextView
    private lateinit var guessGrid: GridLayout
    private val gridCells = mutableListOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        initViews()
        setupGrid()
        setupListeners()
        observeGameState()
    }

    private fun initViews() {
        guessInput = findViewById(R.id.guessInput)
        submitButton = findViewById(R.id.submitButton)
        newGameButton = findViewById(R.id.newGameButton)
        hintsContainer = findViewById(R.id.hintsContainer)
        attemptsText = findViewById(R.id.attemptsText)
        scoreText = findViewById(R.id.scoreText)
        guessGrid = findViewById(R.id.guessGrid)
    }

    private fun setupGrid() {
        // Create 6 rows x 3 columns grid for guesses
        repeat(6) { row ->
            repeat(3) { col ->
                val cell = TextView(this).apply {
                    width = resources.getDimensionPixelSize(R.dimen.grid_cell_size)
                    height = resources.getDimensionPixelSize(R.dimen.grid_cell_size)
                    gravity = Gravity.CENTER
                    textSize = 24f
                    setTextColor(ContextCompat.getColor(context, R.color.colorText))
                    background = ContextCompat.getDrawable(context, R.drawable.grid_cell_background)
                }
                
                val params = GridLayout.LayoutParams().apply {
                    width = GridLayout.LayoutParams.WRAP_CONTENT
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    setMargins(4, 4, 4, 4)
                    rowSpec = GridLayout.spec(row)
                    columnSpec = GridLayout.spec(col)
                }
                
                guessGrid.addView(cell, params)
                gridCells.add(cell)
            }
        }
    }

    private fun setupListeners() {
        submitButton.setOnClickListener {
            val guess = guessInput.text.toString()
            if (guess.length == 3) {
                viewModel.makeGuess(guess.toInt())
                guessInput.text.clear()
            }
        }

        newGameButton.setOnClickListener {
            viewModel.startNewGame()
            guessInput.text.clear()
            submitButton.isEnabled = true
            guessInput.isEnabled = true
            clearGrid()
        }
    }

    private fun observeGameState() {
        lifecycleScope.launch {
            viewModel.gameState.collectLatest { state ->
                when (state) {
                    is GameState.Initial -> {
                        hintsContainer.text = ""
                        attemptsText.text = "Attempts: 0/10"
                        scoreText.text = "Score: 0"
                        submitButton.isEnabled = true
                        guessInput.isEnabled = true
                        clearGrid()
                    }
                    is GameState.Playing -> {
                        updateGrid(state.attempts - 1, state.lastGuess)
                        formatHints(state.hints)
                        attemptsText.text = "Attempts: ${state.attempts}/${state.maxAttempts}"
                        scoreText.text = "Score: ${state.score}"
                        submitButton.isEnabled = true
                        guessInput.isEnabled = true
                    }
                    is GameState.Won -> {
                        hintsContainer.text = "🎉 Congratulations! You won with a score of ${state.score}!"
                        submitButton.isEnabled = false
                        guessInput.isEnabled = false
                    }
                    is GameState.Lost -> {
                        hintsContainer.text = "Game Over! The code was ${state.targetNumber}"
                        submitButton.isEnabled = false
                        guessInput.isEnabled = false
                    }
                }
            }
        }
    }

    private fun updateGrid(row: Int, guess: Int?) {
        guess?.toString()?.padStart(3, '0')?.forEachIndexed { index, digit ->
            val cellIndex = row * 3 + index
            if (cellIndex in gridCells.indices) {
                gridCells[cellIndex].text = digit.toString()
            }
        }
    }

    private fun clearGrid() {
        gridCells.forEach { cell ->
            cell.text = ""
            cell.setBackgroundResource(R.drawable.grid_cell_background)
        }
    }

    private fun formatHints(hints: List<String>) {
        val builder = SpannableStringBuilder()
        hints.forEachIndexed { index, hint ->
            val parts = hint.split(" - ")
            if (parts.size == 2) {
                // Format the number part
                val numberPart = SpannableString(parts[0])
                numberPart.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorText)),
                    0,
                    numberPart.length,
                    0
                )
                
                // Add the formatted parts
                builder.append(numberPart)
                builder.append(" - ${parts[1]}")
                
                // Add new line if not the last hint
                if (index < hints.size - 1) {
                    builder.append("\n\n")
                }
            }
        }
        hintsContainer.text = builder
    }
}
