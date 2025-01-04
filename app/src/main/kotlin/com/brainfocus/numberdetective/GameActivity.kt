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
    private val currentNumbers = IntArray(3) { 0 }
    private var numberPickers: List<NumberPicker> = listOf()
    private lateinit var remainingAttemptsText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        hintsContainer = findViewById(R.id.hintsContainer)
        scoreText = findViewById(R.id.scoreText)
        submitButton = findViewById(R.id.submitButton)
        remainingAttemptsText = findViewById(R.id.remainingAttemptsText)
        
        setupNumberPickers()
        
        submitButton.setOnClickListener {
            val guess = currentNumbers.joinToString("").toInt()
            viewModel.makeGuess(guess)
        }
        
        updateRemainingAttempts(3)
    }

    private fun setupNumberPickers() {
        val pickerContainer = findViewById<LinearLayout>(R.id.numberPickerContainer)
        
        numberPickers = List(3) { index ->
            NumberPicker(this).apply {
                minValue = 0
                maxValue = 9
                wrapSelectorWheel = true
                descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                setOnValueChangedListener { _, _, newVal ->
                    currentNumbers[index] = newVal
                }
            }
        }

        val layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            weight = 1f
            marginEnd = resources.getDimensionPixelSize(R.dimen.picker_margin)
            marginStart = resources.getDimensionPixelSize(R.dimen.picker_margin)
        }

        numberPickers.forEach { picker ->
            pickerContainer.addView(picker, layoutParams)
        }
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
                            updateRemainingAttempts(3 - viewModel.getAttempts())
                            if (viewModel.getAttempts() > 0) {
                                showWrongGuessDialog(3 - viewModel.getAttempts())
                            }
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
            val hintView = layoutInflater.inflate(R.layout.item_hint, hintsContainer, false)
            val numberText = hintView.findViewById<TextView>(R.id.numberText)
            val descriptionText = hintView.findViewById<TextView>(R.id.descriptionText)
            
            numberText.text = hint.numbers.joinToString("  ")
            descriptionText.text = hint.description
            
            hintsContainer.addView(hintView)
        }
    }

    private fun disableInput() {
        submitButton.isEnabled = false
        numberPickers.forEach { it.isEnabled = false }
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

    private fun updateRemainingAttempts(attempts: Int) {
        remainingAttemptsText.text = "Kalan Hak: $attempts"
    }

    private fun showWrongGuessDialog(remainingAttempts: Int) {
        AlertDialog.Builder(this)
            .setTitle("Yanlış Tahmin!")
            .setMessage("Maalesef doğru tahminde bulunamadın.\nKalan hakkın: $remainingAttempts")
            .setPositiveButton("Tamam") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
}
