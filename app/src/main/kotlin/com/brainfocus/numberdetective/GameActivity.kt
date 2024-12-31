package com.brainfocus.numberdetective

import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.brainfocus.numberdetective.viewmodel.GameState
import com.brainfocus.numberdetective.viewmodel.GameViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.brainfocus.numberdetective.viewmodel.Hint
import android.view.Gravity
import androidx.appcompat.app.AlertDialog
import android.widget.NumberPicker
import android.widget.LinearLayout
import kotlinx.coroutines.flow.combine
import android.graphics.Typeface
import android.content.Intent

class GameActivity : AppCompatActivity() {
    private val viewModel: GameViewModel by viewModel()
    private lateinit var hintsContainer: LinearLayout
    private lateinit var scoreText: TextView
    private lateinit var submitButton: Button
    private lateinit var numberTexts: List<TextView>
    private var currentNumbers = mutableListOf(0, 0, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        
        setupViews()
        observeViewModel()
        viewModel.startNewGame()
    }

    private fun setupViews() {
        hintsContainer = findViewById(R.id.hintsContainer)
        scoreText = findViewById(R.id.scoreText)
        submitButton = findViewById(R.id.submitButton)
        
        numberTexts = listOf(
            findViewById(R.id.numberText1),
            findViewById(R.id.numberText2),
            findViewById(R.id.numberText3)
        )
        
        numberTexts.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                showNumberPicker(index)
            }
        }
        
        submitButton.setOnClickListener {
            val guess = currentNumbers.joinToString("").toInt()
            viewModel.makeGuess(guess)
        }
    }

    private fun showNumberPicker(index: Int) {
        val builder = AlertDialog.Builder(this, R.style.NumberPickerDialog)
        val numberPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = 9
            value = currentNumbers[index]
            wrapSelectorWheel = true
        }
        
        builder.setView(numberPicker)
            .setPositiveButton("Tamam") { _, _ ->
                currentNumbers[index] = numberPicker.value
                numberTexts[index].text = numberPicker.value.toString()
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.hints.combine(viewModel.gameState) { hints, state ->
                    Pair(hints, state)
                }.collect { (hints, state) ->
                    updateHints(hints)
                    
                    when (state) {
                        is GameState.Won -> {
                            disableInput()
                            showGameResult(true, state.score)
                        }
                        is GameState.Lost -> {
                            disableInput()
                            showGameResult(false, 0)
                        }
                        is GameState.Playing -> {
                            updateScore(state.score)
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun updateHints(hints: List<Hint>) {
        hintsContainer.removeAllViews()
        
        hints.forEach { hint ->
            val hintLayout = layoutInflater.inflate(R.layout.hint_item, hintsContainer, false)
            
            val numbersContainer = hintLayout.findViewById<LinearLayout>(R.id.numbersContainer)
            val hintDescription = hintLayout.findViewById<TextView>(R.id.hintDescription)
            
            hint.numbers.forEach { number ->
                val numberBox = TextView(this).apply {
                    text = number.toString()
                    textSize = 24f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(getColor(R.color.colorText))
                    gravity = Gravity.CENTER
                    background = getDrawable(R.drawable.grid_cell_background)
                    layoutParams = LinearLayout.LayoutParams(72, 72).apply {
                        marginEnd = 20
                    }
                }
                numbersContainer.addView(numberBox)
            }
            
            hintDescription.apply {
                text = hint.description
                textSize = 16f
                setTextColor(getColor(R.color.colorTextSecondary))
            }
            hintsContainer.addView(hintLayout)
        }
    }

    private fun disableInput() {
        submitButton.isEnabled = false
        numberTexts.forEach { it.isEnabled = false }
    }

    private fun updateScore(score: Int) {
        scoreText.text = score.toString()
    }

    private fun showGameResult(isWin: Boolean, score: Int) {
        val intent = Intent(this, GameResultActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("is_win", isWin)
            putExtra("score", score)
            putExtra("attempts", viewModel.getAttempts())
            putExtra("time_seconds", viewModel.getGameTime())
        }
        
        startActivity(intent)
    }
}
