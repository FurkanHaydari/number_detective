package com.brainfocus.numberdetective

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.brainfocus.numberdetective.viewmodel.GameViewModel
import com.brainfocus.numberdetective.viewmodel.GameState

class GameActivity : AppCompatActivity() {

    private val viewModel: GameViewModel by viewModel()
    private lateinit var guessGrid: GridLayout
    private lateinit var numpad: GridLayout
    private lateinit var hintsViewPager: ViewPager2
    private lateinit var hintTabLayout: TabLayout
    private lateinit var prevHintButton: ImageButton
    private lateinit var nextHintButton: ImageButton
    private lateinit var attemptsProgress: LinearProgressIndicator
    private lateinit var scoreText: TextView
    private var currentRow = 0
    private var currentCol = 0
    private var currentScore = 1000
    private val usedDigits = mutableSetOf<Int>()
    private val numpadButtons = mutableMapOf<Int, MaterialButton>()
    private lateinit var hintAdapter: HintPagerAdapter

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
        hintsViewPager = findViewById(R.id.hintsViewPager)
        hintTabLayout = findViewById(R.id.hintTabLayout)
        prevHintButton = findViewById<ImageButton>(R.id.prevHintButton)
        nextHintButton = findViewById<ImageButton>(R.id.nextHintButton)
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
        hintAdapter = HintPagerAdapter()
        hintsViewPager.adapter = hintAdapter
        
        // Connect TabLayout with ViewPager2
        TabLayoutMediator(hintTabLayout, hintsViewPager) { tab, _ ->
            tab.icon = ContextCompat.getDrawable(this, R.drawable.tab_indicator)
        }.attach()

        // Setup navigation buttons
        prevHintButton.setOnClickListener {
            if (hintsViewPager.currentItem > 0) {
                hintsViewPager.currentItem = hintsViewPager.currentItem - 1
            }
        }

        nextHintButton.setOnClickListener {
            if (hintsViewPager.currentItem < hintAdapter.itemCount - 1) {
                hintsViewPager.currentItem = hintsViewPager.currentItem + 1
            }
        }

        // Update button states when page changes
        hintsViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                prevHintButton.isEnabled = position > 0
                nextHintButton.isEnabled = position < hintAdapter.itemCount - 1
                
                prevHintButton.alpha = if (position > 0) 1.0f else 0.5f
                nextHintButton.alpha = if (position < hintAdapter.itemCount - 1) 1.0f else 0.5f
            }
        })
        
        // Initial empty state - will be populated by updatePlayingState
        val initialHints = listOf(
            HintPagerAdapter.HintItem(
                "Waiting for game to start...",
                "000"
            )
        )
        hintAdapter.updateHints(initialHints)
    }

    private fun setupNumpad() {
        numpad.removeAllViews()
        for (i in 0..9) {
            val button = MaterialButton(this).apply {
                id = View.generateViewId()
                text = i.toString()
                textSize = 24f
                layoutParams = GridLayout.LayoutParams().apply {
                    width = resources.getDimensionPixelSize(R.dimen.numpad_button_size)
                    height = resources.getDimensionPixelSize(R.dimen.numpad_button_size)
                    setMargins(8, 8, 8, 8)
                }
                setBackgroundResource(R.drawable.button_background)
                setOnClickListener { onNumberClick(i) }
            }
            if (i == 0) {
                (button.layoutParams as GridLayout.LayoutParams).columnSpec = GridLayout.spec(1)
            }
            numpadButtons[i] = button
            numpad.addView(button)
        }
    }

    private fun onNumberClick(number: Int) {
        if (currentCol < 3 && !usedDigits.contains(number)) {
            val cell = guessGrid.findViewWithTag<TextView>("cell_${currentRow}_$currentCol")
            cell.text = number.toString()
            usedDigits.add(number)
            
            // Disable the clicked button
            numpadButtons[number]?.isEnabled = false
            
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
                
                // Reset used digits and enable all buttons for next guess
                usedDigits.clear()
                numpadButtons.values.forEach { button ->
                    button.isEnabled = true
                }
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
        // Update score
        currentScore = state.score
        scoreText.text = "Score: $currentScore"
        
        // Update hints
        val hintItems = state.hints.map { hint ->
            val parts = hint.split(" - ")
            val number = parts[0]
            val description = parts[1]
            HintPagerAdapter.HintItem(description, number)
        }
        hintAdapter.updateHints(hintItems)
        
        // Update attempts progress
        attemptsProgress.progress = state.attempts
        
        // Update colors for the last guess if available
        state.lastGuess?.let { updateGridColors(it) }
    }

    private fun updateGridColors(guess: Int) {
        val targetStr = viewModel.targetNumber.toString().padStart(3, '0')
        val guessStr = guess.toString().padStart(3, '0')
        val cells = mutableListOf<TextView>()
        
        // Collect cells
        for (col in 0..2) {
            val cell = guessGrid.findViewWithTag<TextView>("cell_${currentRow - 1}_$col")
            cells.add(cell)
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
}
