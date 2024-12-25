package com.brainfocus.numberdetective

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.brainfocus.numberdetective.viewmodel.GameViewModel
import com.brainfocus.numberdetective.viewmodel.GameState

class GameActivity : AppCompatActivity() {

    private val viewModel: GameViewModel by viewModel()
    private lateinit var guessGrid: GridLayout
    private lateinit var numpad: GridLayout
    private lateinit var hintsContainer: LinearLayout
    private lateinit var attemptsProgress: LinearProgressIndicator
    private lateinit var scoreText: TextView
    private var currentRow = 0
    private var currentCol = 0
    private var currentScore = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        setupViews()
        setupGrid()
        setupHints()
        setupNumpad()
        observeGameState()
        
        // Start a new game
        viewModel.startNewGame()

        // Handle back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun setupViews() {
        guessGrid = findViewById(R.id.guessGrid)
        numpad = findViewById(R.id.numpad)
        hintsContainer = findViewById(R.id.hintsContainer)
        attemptsProgress = findViewById(R.id.attemptsProgress)
        scoreText = findViewById(R.id.scoreText)

        attemptsProgress.max = 3
        scoreText.text = "Score: $currentScore"
    }

    private fun setupGrid() {
        guessGrid.removeAllViews()
        for (row in 0 until 3) {
            for (col in 0 until 3) {
                val cell = TextView(this).apply {
                    id = resources.getIdentifier("cell_${row}_$col", "id", packageName)
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = resources.getDimensionPixelSize(R.dimen.grid_cell_size)
                        height = resources.getDimensionPixelSize(R.dimen.grid_cell_size)
                        setMargins(8, 8, 8, 8)
                    }
                    gravity = Gravity.CENTER
                    setBackgroundResource(R.drawable.grid_cell_background)
                    textSize = 24f
                    setTextColor(ContextCompat.getColor(context, R.color.colorText))
                    tag = "cell_${row}_$col"
                }
                guessGrid.addView(cell)
            }
        }
    }

    private fun setupHints() {
        hintsContainer.removeAllViews()
        // Add 5 hint rows initially
        for (i in 0 until 5) {
            val hintRow = layoutInflater.inflate(R.layout.hint_row, hintsContainer, false)
            val hintText = hintRow.findViewById<TextView>(R.id.hintText)
            hintText.visibility = View.VISIBLE
            hintText.text = "Hint ${i + 1}"
            
            // Initialize circles with default background
            val circles = listOf(
                hintRow.findViewById<View>(R.id.hintCircle1),
                hintRow.findViewById<View>(R.id.hintCircle2),
                hintRow.findViewById<View>(R.id.hintCircle3)
            )
            circles.forEach { circle ->
                circle.setBackgroundResource(R.drawable.circle_background)
            }
            
            hintsContainer.addView(hintRow)
        }
    }

    private fun setupNumpad() {
        numpad.removeAllViews()
        val numbers = (1..9).toList() + listOf(0)
        
        numbers.forEach { number ->
            val button = MaterialButton(this).apply {
                text = number.toString()
                textSize = 22f
                setTextColor(ContextCompat.getColor(context, R.color.white))
                setBackgroundResource(R.drawable.button_background)
                layoutParams = GridLayout.LayoutParams().apply {
                    width = resources.getDimensionPixelSize(R.dimen.numpad_button_size)
                    height = resources.getDimensionPixelSize(R.dimen.numpad_button_size)
                    setMargins(4, 4, 4, 4)
                }
                setOnClickListener { onNumberClick(number) }
                elevation = 4f
                strokeWidth = 2
                setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.neonBlue)))
            }
            numpad.addView(button)
        }
    }

    private fun onNumberClick(number: Int) {
        if (currentCol < 3) {
            val cell = guessGrid.findViewWithTag<TextView>("cell_${currentRow}_$currentCol")
            cell.text = number.toString()
            currentCol++
            
            if (currentCol == 3) {
                // Get the guess from the current row
                val guess = StringBuilder()
                for (col in 0..2) {
                    val guessCell = guessGrid.findViewWithTag<TextView>("cell_${currentRow}_$col")
                    guess.append(guessCell.text)
                }
                
                viewModel.makeGuess(guess.toString().toInt())
                currentCol = 0
                currentRow++
            }
        }
    }

    private fun animateScore(newScore: Int) {
        val animator = ValueAnimator.ofInt(currentScore, newScore)
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            scoreText.text = "Score: $animatedValue"
        }
        animator.start()
        currentScore = newScore
    }

    private fun updateGuessColors(row: Int, correctCount: Int, misplacedCount: Int) {
        val targetStr = viewModel.targetNumber.toString().padStart(3, '0')
        val guessStr = StringBuilder()
        val cells = mutableListOf<TextView>()
        
        // Collect guess and cells
        for (col in 0..2) {
            val cell = guessGrid.findViewWithTag<TextView>("cell_${row}_$col")
            cells.add(cell)
            guessStr.append(cell.text)
        }

        // First mark correct positions
        cells.forEachIndexed { index, cell ->
            if (guessStr[index] == targetStr[index]) {
                cell.setBackgroundResource(R.drawable.grid_cell_correct)
            }
        }

        // Then mark misplaced positions
        cells.forEachIndexed { index, cell ->
            if (guessStr[index] != targetStr[index] && targetStr.contains(guessStr[index])) {
                cell.setBackgroundResource(R.drawable.grid_cell_misplaced)
            }
        }

        // Finally mark incorrect positions
        cells.forEachIndexed { index, cell ->
            if (!targetStr.contains(guessStr[index])) {
                cell.setBackgroundResource(R.drawable.grid_cell_incorrect)
            }
        }
    }

    private fun observeGameState() {
        lifecycleScope.launch {
            viewModel.gameState.collect { state ->
                when (state) {
                    is GameState.Initial -> {
                        // Reset UI for new game
                        currentRow = 0
                        currentCol = 0
                        currentScore = 1000
                        scoreText.text = "Score: $currentScore"
                        
                        // Clear grid
                        for (row in 0 until 3) {
                            for (col in 0 until 3) {
                                val cell = guessGrid.findViewWithTag<TextView>("cell_${row}_$col")
                                cell.text = ""
                                cell.setBackgroundResource(R.drawable.grid_cell_background)
                            }
                        }
                    }
                    is GameState.Playing -> updatePlayingState(state)
                    is GameState.Won -> {
                        animateScore(state.score)
                        // TODO: Add winning animation
                    }
                    is GameState.Lost -> {
                        // TODO: Add losing animation
                    }
                }
            }
        }
    }

    private fun updatePlayingState(state: GameState.Playing) {
        attemptsProgress.progress = state.attempts
        attemptsProgress.max = state.maxAttempts
        animateScore(state.score)
        
        // Update hints text
        state.hints.forEachIndexed { index, hint ->
            if (index >= hintsContainer.childCount) return@forEachIndexed
            
            val hintRow = hintsContainer.getChildAt(index) as? ViewGroup ?: return@forEachIndexed
            val hintText = hintRow.findViewById<TextView>(R.id.hintText) ?: return@forEachIndexed
            hintText.text = hint
        }
        
        // Update colors for the last guess
        state.lastGuess?.let { guess ->
            if (state.attempts <= 0 || state.attempts > 3) return@let
            
            val guessStr = guess.toString().padStart(3, '0')
            val targetStr = viewModel.targetNumber.toString().padStart(3, '0')
            var correctCount = 0
            var misplacedCount = 0

            // First count exact matches
            guessStr.forEachIndexed { index, digit ->
                if (digit == targetStr[index]) {
                    correctCount++
                }
            }

            // Then count misplaced digits
            guessStr.forEach { digit ->
                when {
                    targetStr.count { targetDigit -> targetDigit == digit } > 
                        guessStr.filterIndexed { index, d -> 
                            d == digit && guessStr[index] == targetStr[index] 
                        }.count() -> misplacedCount++
                }
            }

            // Update the guess colors
            updateGuessColors(state.attempts - 1, correctCount, misplacedCount)
        }
    }
}
