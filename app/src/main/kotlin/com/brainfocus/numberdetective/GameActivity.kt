package com.brainfocus.numberdetective

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.brainfocus.numberdetective.adapter.HintAdapter
import com.brainfocus.numberdetective.databinding.ActivityGameBinding
import com.brainfocus.numberdetective.model.GameState
import com.brainfocus.numberdetective.model.GuessResult
import com.brainfocus.numberdetective.sound.SoundManager
import com.brainfocus.numberdetective.ui.FallDownItemAnimator
import com.brainfocus.numberdetective.ui.HintItemDecoration
import com.brainfocus.numberdetective.viewmodel.GameViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class GameActivity : AppCompatActivity(), NumberPicker.OnValueChangeListener {
    private lateinit var binding: ActivityGameBinding
    private val viewModel: GameViewModel by viewModels()
    private lateinit var hintAdapter: HintAdapter
    private var isAnimating = false
    private lateinit var numberPickers: List<NumberPicker>

    @Inject
    lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        makeFullScreen()

        lifecycleScope.launch {
            withContext(kotlinx.coroutines.Dispatchers.IO) {
                soundManager.initialize()
            }
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                setupViews()
                setupUI()
                setupObservers()
            }
        }
    }

    private fun setupUI() {
        hintAdapter = HintAdapter()
        binding.hintsContainer.apply {
            adapter = hintAdapter
            layoutManager = LinearLayoutManager(this@GameActivity)
            itemAnimator = FallDownItemAnimator()
            addItemDecoration(HintItemDecoration(8))
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.hints.collect { hints ->
                    hintAdapter.submitList(hints)
                }
            }
        }

        binding.submitButton.setOnClickListener {
            val guess = numberPickers.joinToString("") { it.value.toString() }
            handleGuess(guess)
        }
    }

    private fun setupViews() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 3f
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
        }

        numberPickers = listOf(
            NumberPicker(this).apply {
                id = View.generateViewId()
                minValue = 0
                maxValue = 9
                setOnValueChangedListener(this@GameActivity)
                wrapSelectorWheel = true 
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            },
            NumberPicker(this).apply {
                id = View.generateViewId()
                minValue = 0
                maxValue = 9
                setOnValueChangedListener(this@GameActivity)
                wrapSelectorWheel = true 
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            },
            NumberPicker(this).apply {
                id = View.generateViewId()
                minValue = 0
                maxValue = 9
                setOnValueChangedListener(this@GameActivity)
                wrapSelectorWheel = true 
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
        )

        numberPickers.forEach { picker ->
            container.addView(picker)
        }

        binding.numberPickerContainer.addView(container)

        binding.apply {
            submitButton.alpha = 0f
            submitButton.translationY = 100f
            hintsCard.alpha = 0f
            hintsCard.translationX = -100f

            container.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    hintsCard.animate()
                        .alpha(1f)
                        .translationX(0f)
                        .setDuration(400)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                    
                    submitButton.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(400)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }
                .start()
        }

        binding.submitButton.setOnClickListener {
            val guess = numberPickers.joinToString("") { it.value.toString() }
            handleGuess(guess)
        }
    }

    private fun handleGuess(guess: String) {
        if (guess.isBlank()) {
            showToast("Lütfen bir tahmin girin")
            return
        }

        if (!isValidGuess(guess)) {
            showToast("Geçersiz tahmin! Her rakam farklı olmalı")
            return
        }

        if (viewModel.guesses.value.contains(guess)) {
            showToast("Bu tahmini daha önce yaptınız")
            return
        }

        val result = viewModel.makeGuess(guess)
        
        when (result) {
            is GuessResult.Correct -> {
                soundManager.playWinSound()
                showGameOverDialog(true)
            }
            is GuessResult.Wrong -> {
                soundManager.playWrongSound()
                if (viewModel.wrongAttempts.value < 3) {
                    showWrongGuessAlert()
                }
            }
            is GuessResult.Partial -> {
                soundManager.playPartialSound()
                showPartialMatchInfo(result.correctDigits, result.wrongPositionDigits)
            }
        }

        if (viewModel.wrongAttempts.value >= 3 || viewModel.remainingAttempts.value <= 0) {
            showGameOverDialog(false)
            return
        }

        smoothlyResetPickers()
    }

    private fun showPartialMatchInfo(correctDigits: Int, wrongPositionDigits: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle("İpucu")
            .setMessage("Doğru rakam ve yeri: $correctDigits\nDoğru rakam yanlış yer: $wrongPositionDigits")
            .setPositiveButton("Tamam") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showWrongGuessAlert() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Yanlış Tahmin!")
            .setMessage("Hiç eşleşme yok.")
            .setPositiveButton("Tamam") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showGameOverDialog(isWin: Boolean) {
        val intent = Intent(this, GameResultActivity::class.java).apply {
            putExtra("score", viewModel.score.value)
            putExtra("isWin", isWin)
            putExtra("correctAnswer", viewModel.correctAnswer.value)
            putStringArrayListExtra("guesses", ArrayList(viewModel.guesses.value))
            putExtra("attempts", viewModel.attempts)
            putExtra("timeInSeconds", viewModel.getTimeInSeconds())
        }
        startActivity(intent)
        finish()
    }

    private fun isValidGuess(guess: String): Boolean {
        if (guess.length != 3 || !guess.all { it.isDigit() }) {
            return false
        }

        val digits = guess.toSet()
        return digits.size == 3 
    }

    private fun smoothlyResetPickers() {
        val duration = 500L 
        
        numberPickers.forEach { picker ->
            val currentValue = picker.value
            ValueAnimator.ofInt(currentValue, 0).apply {
                this.duration = duration
                addUpdateListener { animator ->
                    picker.value = animator.animatedValue as Int
                }
                interpolator = DecelerateInterpolator()
                start()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.hints.collect { hints ->
                        if (hints.isNotEmpty()) {
                            hintAdapter.submitList(hints)
                        }
                    }
                }

                launch {
                    viewModel.wrongAttempts.collect { wrongAttempts ->
                        if (wrongAttempts > 0) {
                            binding.scoreText.text = getString(R.string.score_text, viewModel.score.value)
                        }
                    }
                }

                launch {
                    viewModel.score.collect { scoreValue ->
                        binding.scoreText.text = getString(R.string.score_text, scoreValue)
                    }
                }

                launch {
                    viewModel.gameState.collect { gameState ->
                        when (gameState) {
                            is GameState.GameOver -> showGameOverDialog(false)
                            is GameState.Win -> showGameOverDialog(true)
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun makeFullScreen() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
        soundManager.playTickSound()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
