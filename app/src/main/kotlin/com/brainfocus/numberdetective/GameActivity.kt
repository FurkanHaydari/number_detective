package com.brainfocus.numberdetective

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.brainfocus.numberdetective.adapter.HintAdapter
import com.brainfocus.numberdetective.databinding.ActivityGameBinding
import com.brainfocus.numberdetective.databinding.ItemHintSquaresBinding
import com.brainfocus.numberdetective.model.GameState
import com.brainfocus.numberdetective.model.GuessResult
import com.brainfocus.numberdetective.model.Hint
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
        hintAdapter = HintAdapter { hint ->
            showHintDetail(hint)
        }
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

        // Overlay'e tıklandığında detay görünümünü kapat
        binding.hintDetailOverlay.setOnClickListener {
            hideHintDetail()
        }

        // Detay kartına tıklandığında olayın overlay'e geçmesini engelle
        binding.hintDetailCard.setOnClickListener { }
    }

    private fun setupViews() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = if (viewModel.currentLevel.value == 3) 4f else 3f
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
        }

        val numPickers = if (viewModel.currentLevel.value == 3) 4 else 3
        
        numberPickers = List(numPickers) { 
            NumberPicker(this).apply {
                id = View.generateViewId()
                minValue = 0
                maxValue = 9
                setOnValueChangedListener(this@GameActivity)
                wrapSelectorWheel = true 
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
        }

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
        // Boş tahmin kontrolü
        if (guess.isBlank()) {
            Toast.makeText(this, "Lütfen bir tahmin girin", Toast.LENGTH_SHORT).show()
            return
        }

        // Geçerli tahmin kontrolü
        if (!isValidGuess(guess)) {
            Toast.makeText(this, "Geçersiz tahmin! Her rakam farklı olmalı", Toast.LENGTH_SHORT).show()
            binding.numberPickerCard.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            return
        }

        // Tahmin uzunluğu kontrolü
        val expectedLength = if (viewModel.currentLevel.value == 3) 4 else 3
        if (guess.length != expectedLength) {
            Toast.makeText(this, "Tahmin $expectedLength basamaklı olmalıdır", Toast.LENGTH_SHORT).show()
            return
        }

        // Tekrar eden tahmin kontrolü
        if (viewModel.guesses.value.contains(guess)) {
            Toast.makeText(this, "Bu tahmini daha önce yaptınız", Toast.LENGTH_SHORT).show()
            binding.numberPickerCard.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            return
        }

        // Tahmin sonucunu işle
        val result = viewModel.makeGuess(guess)
        when (result) {
            is GuessResult.Correct -> {
                soundManager.playWinSound()
                if (viewModel.currentLevel.value >= GameViewModel.MAX_LEVELS) {
                    Toast.makeText(this, "Tebrikler! Oyunu kazandınız!", Toast.LENGTH_LONG).show()
                    navigateToGameResult(true)
                } else {
                    viewModel.nextLevel()
                    Toast.makeText(this, "Tebrikler! ${viewModel.currentLevel.value}. seviyeye geçtiniz", Toast.LENGTH_SHORT).show()
                }
            }
            is GuessResult.Wrong -> {
                soundManager.playWrongSound()
                binding.numberPickerCard.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                val remainingAttempts = viewModel.remainingAttempts.value
                if (remainingAttempts > 0) {
                    Toast.makeText(this, "Yanlış tahmin! $remainingAttempts hakkınız kaldı", Toast.LENGTH_SHORT).show()
                }
                if (viewModel.wrongAttempts.value >= 3 || remainingAttempts <= 0) {
                    Toast.makeText(this, "Oyun bitti! Doğru cevap: ${viewModel.correctAnswer.value}", Toast.LENGTH_LONG).show()
                    navigateToGameResult(false)
                    return
                }
            }
            is GuessResult.Partial -> {
                soundManager.playWrongSound()
                binding.numberPickerCard.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                val message = when {
                    result.correctDigits > 0 && result.wrongPositionDigits > 0 -> 
                        "${result.correctDigits} rakam doğru yerde, ${result.wrongPositionDigits} rakam yanlış yerde"
                    result.correctDigits > 0 -> 
                        "${result.correctDigits} rakam doğru yerde"
                    result.wrongPositionDigits > 0 -> 
                        "${result.wrongPositionDigits} rakam yanlış yerde"
                    else -> 
                        "Hiç eşleşme yok"
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }

        animateNumberPickers()
    }

    private fun navigateToGameResult(isWin: Boolean) {
        val intent = Intent(this, GameResultActivity::class.java).apply {
            putExtra("score", viewModel.score.value)
            putExtra("isWin", isWin)
            putExtra("correctAnswer", viewModel.correctAnswer.value)
            putExtra("attempts", viewModel.attempts)
            putExtra("timeInSeconds", viewModel.getTimeInSeconds().toLong())
            putStringArrayListExtra("guesses", ArrayList(viewModel.guesses.value))
        }
        startActivity(intent)
        finish()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.remainingAttempts.collect { attempts ->
                        binding.remainingAttemptsTextTop.text = "❤️ $attempts"
                    }
                }

                launch {
                    viewModel.currentLevel.collect { level ->
                        binding.levelText.text = "Level $level"
                        // Level değiştiğinde number picker'ları güncelle
                        binding.numberPickerContainer.removeAllViews()
                        setupViews()
                    }
                }

                launch {
                    viewModel.remainingTime.collect { seconds ->
                        val minutes = seconds / 60
                        val remainingSeconds = seconds % 60
                        binding.timerText.text = String.format("%d:%02d", minutes, remainingSeconds)
                    }
                }

                launch {
                    viewModel.gameState.collect { state ->
                        when (state) {
                            is GameState.Win -> {
                                soundManager.playWinSound()
                                navigateToGameResult(true)
                            }
                            is GameState.GameOver -> {
                                soundManager.playWrongSound()
                                navigateToGameResult(false)
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun isValidGuess(guess: String): Boolean {
        val expectedLength = if (viewModel.currentLevel.value == 3) 4 else 3
        if (guess.length != expectedLength || !guess.all { it.isDigit() }) {
            return false
        }

        val digits = guess.toSet()
        return digits.size == expectedLength
    }

    private fun animateNumberPickers() {
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

    private fun showHintDetail(hint: Hint) {
        // Detay kartını güncelle
        binding.hintDetailCard.findViewById<ConstraintLayout>(R.id.container)?.removeAllViews()
        val detailBinding = ItemHintSquaresBinding.inflate(layoutInflater)
        
        // Hint verilerini detay görünümüne aktar
        with(detailBinding) {
            square1.text = hint.guess[0].toString()
            square2.text = hint.guess[1].toString()
            square3.text = hint.guess[2].toString()
            square4.visibility = if (hint.guess.length == 4) {
                square4.text = hint.guess[3].toString()
                View.VISIBLE
            } else {
                View.GONE
            }
            hintDescription.text = hint.description

            // Detay görünümü için boyutları ve görünümü ayarla
            val cardContainer = root
            cardContainer.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            // Kare boyutlarını büyüt
            listOf(square1, square2, square3, square4).forEach { square ->
                square.layoutParams = square.layoutParams.apply {
                    width = (resources.displayMetrics.density * 60).toInt() // 60dp
                    height = (resources.displayMetrics.density * 60).toInt() // 60dp
                }
                square.textSize = 28f // Daha büyük font
            }

            // Açıklama metnini büyüt
            hintDescription.textSize = 18f
            hintDescription.setPadding(0, 
                (resources.displayMetrics.density * 16).toInt(), // 16dp
                0, 0)
        }

        binding.hintDetailCard.findViewById<ConstraintLayout>(R.id.container)?.addView(detailBinding.root)

        // Overlay'i göster ve animasyonla aç
        binding.hintDetailOverlay.apply {
            alpha = 0f
            visibility = View.VISIBLE
            
            animate()
                .alpha(1f)
                .setDuration(300)
                .withStartAction {
                    // Detay kartını scale ve fade in animasyonu
                    binding.hintDetailCard.apply {
                        scaleX = 0.8f
                        scaleY = 0.8f
                        alpha = 0f
                        animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setDuration(300)
                            .setInterpolator(DecelerateInterpolator())
                            .start()
                    }
                }
                .start()
        }
    }

    private fun hideHintDetail() {
        // Animasyonla kapat
        binding.hintDetailOverlay.animate()
            .alpha(0f)
            .setDuration(250)
            .withStartAction {
                // Detay kartını scale ve fade out animasyonu
                binding.hintDetailCard.animate()
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .alpha(0f)
                    .setDuration(200)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
            .withEndAction {
                binding.hintDetailOverlay.visibility = View.GONE
                binding.hintDetailCard.findViewById<ConstraintLayout>(R.id.container)?.removeAllViews()
            }
            .start()
    }
}
