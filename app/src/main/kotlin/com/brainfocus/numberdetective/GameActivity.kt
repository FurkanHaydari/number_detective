package com.brainfocus.numberdetective

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.adapter.HintAdapter
import com.brainfocus.numberdetective.decoration.HintItemDecoration
import com.brainfocus.numberdetective.model.GuessResult
import com.brainfocus.numberdetective.sound.SoundManager
import com.brainfocus.numberdetective.viewmodel.GameState
import com.brainfocus.numberdetective.viewmodel.GameViewModel
import com.brainfocus.numberdetective.viewmodel.Hint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

import android.os.Build
import android.util.Log
import android.media.MediaPlayer

class GameActivity : AppCompatActivity() {
    private val viewModel: GameViewModel by viewModel()
    private lateinit var hintsContainer: RecyclerView
    private lateinit var hintsAdapter: HintAdapter
    private lateinit var scoreText: TextView
    private lateinit var submitButton: Button
    private var numberPickers: List<NumberPicker> = listOf()
    private lateinit var remainingAttemptsText: TextView
    private lateinit var soundManager: SoundManager
    private var pendingGameResult: Intent? = null
    private var attemptsList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        setupFullscreen()
        
        soundManager = SoundManager(this)
        // Load sound after a short delay to ensure resources are ready
        Handler(Looper.getMainLooper()).postDelayed({
            soundManager.loadSound(R.raw.tick_sound)
        }, 500)
        
        setupViews()
        observeViewModel()
        startScoreTimer()
    }

    private fun setupFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }
    }

    private fun setupViews() {
        hintsContainer = findViewById(R.id.hintsContainer)
        scoreText = findViewById(R.id.scoreText)
        submitButton = findViewById(R.id.submitButton)
        remainingAttemptsText = findViewById(R.id.remainingAttemptsText)
        
        setupNumberPickers()
        
        setupSubmitButton()
        
        hintsAdapter = HintAdapter()
        hintsContainer.adapter = hintsAdapter
        hintsContainer.addItemDecoration(HintItemDecoration(dpToPx(8)))
        
        updateRemainingAttempts(3)
    }

    private fun setupNumberPickers() {
        val pickerContainer = findViewById<LinearLayout>(R.id.numberPickerContainer)
        
        val isDarkMode = resources.configuration.uiMode and 
            android.content.res.Configuration.UI_MODE_NIGHT_MASK == 
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        
        numberPickers = List(3) { index ->
            NumberPicker(this).apply {
                minValue = 0
                maxValue = 9
                value = 0  // Başlangıç değeri
                wrapSelectorWheel = true
                descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                
                val params = LinearLayout.LayoutParams(
                    dpToPx(65),
                    dpToPx(130)
                ).apply {
                    weight = 1f
                    marginStart = if (index > 0) dpToPx(16) else 0
                }
                layoutParams = params

                if (isDarkMode) {
                    setBackgroundResource(R.drawable.number_picker_background_dark)
                    setDividerColor(ContextCompat.getColor(context, R.color.colorDividerDark))
                } else {
                    setBackgroundResource(R.drawable.number_picker_background)
                    setDividerColor(ContextCompat.getColor(context, R.color.colorDivider))
                }
                
                alpha = 0.7f
                
                var lastPlayTime = 0L
                val MIN_SOUND_INTERVAL = 50L // Minimum 50ms between sounds
                
                setOnScrollListener(object : NumberPicker.OnScrollListener {
                    override fun onScrollStateChange(view: NumberPicker?, scrollState: Int) {
                        when (scrollState) {
                            NumberPicker.OnScrollListener.SCROLL_STATE_FLING -> {
                                // Play initial fling sound
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastPlayTime >= MIN_SOUND_INTERVAL) {
                                    soundManager.playSound(R.raw.tick_sound)
                                    lastPlayTime = currentTime
                                }
                            }
                        }
                    }
                })
                
                setOnValueChangedListener(object : NumberPicker.OnValueChangeListener {
                    override fun onValueChange(picker: NumberPicker, oldVal: Int, newVal: Int) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastPlayTime >= MIN_SOUND_INTERVAL) {
                            soundManager.playSound(R.raw.tick_sound)
                            lastPlayTime = currentTime
                        }
                    }
                })
            }
        }
        
        numberPickers.forEach { picker ->
            pickerContainer.addView(picker)
        }
    }

    private fun setDividerColor(color: Int) {
        val pickerFields = NumberPicker::class.java.declaredFields
        pickerFields.forEach { field ->
            if (field.name.contains("Divider", ignoreCase = true)) {
                field.isAccessible = true
                try {
                    field.set(this, ColorDrawable(color))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun animateValueChange(picker: NumberPicker) {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 150
        animator.interpolator = AccelerateDecelerateInterpolator()
        
        animator.addUpdateListener { animation ->
            picker.alpha = 0.7f + (animation.animatedValue as Float) * 0.3f
        }
        
        animator.start()
        picker.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.gameState.collect { state ->
                    handleGameState(state)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.hints.collect { hints ->
                    updateHints(hints)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.score.collect { score ->
                    updateScore(score)
                }
            }
        }
    }

    private fun handleGameState(state: GameState) {
        when (state) {
            is GameState.Won -> {
                soundManager.playSound(R.raw.win_sound)
                disableInput()
                val intent = Intent(this, GameResultActivity::class.java).apply {
                    putExtra("score", state.score)
                    putExtra("result", "win")
                    putExtra("correctAnswer", viewModel.getCorrectAnswer())
                    putExtra("attempts", viewModel.getAttempts())
                    putExtra("gameTime", viewModel.getGameTime())
                    putStringArrayListExtra("attemptsList", ArrayList(attemptsList))
                }
                startActivity(intent)
                finish()
            }
            is GameState.Lost -> {
                soundManager.playSound(R.raw.lose_sound)
                disableInput()
                val intent = Intent(this, GameResultActivity::class.java).apply {
                    putExtra("score", viewModel.score.value)
                    putExtra("result", "lose")
                    putExtra("correctAnswer", viewModel.getCorrectAnswer())
                    putExtra("attempts", viewModel.getAttempts())
                    putExtra("gameTime", viewModel.getGameTime())
                    putStringArrayListExtra("attemptsList", ArrayList(attemptsList))
                }
                startActivity(intent)
                finish()
            }
            is GameState.Playing -> {
                soundManager.playSound(R.raw.wrong_guess)
                submitButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))
                showErrorDialog("Üzgünüm, bu doğru tahmin değil. Tekrar deneyin! ")
                updateRemainingAttempts(3 - viewModel.getAttempts())
            }
            is GameState.Error -> {
                showErrorDialog(state.message)
            }
            GameState.Initial -> {
                // Initial state, no action needed
            }
        }
    }

    private fun setupSubmitButton() {
        submitButton.apply {
            setBackgroundResource(R.drawable.red_button_background)
            stateListAnimator = null
            isHapticFeedbackEnabled = true
            alpha = 1f
            elevation = 0f
            outlineProvider = null
            backgroundTintList = null
            
            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.animate()
                            .scaleX(0.98f)
                            .scaleY(0.98f)
                            .alpha(0.9f)
                            .setDuration(100)
                            .start()
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setDuration(100)
                            .start()
                    }
                }
                false
            }
            
            setOnClickListener {
                soundManager.playSound(R.raw.button_click)
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(50)
                    .withEndAction {
                        animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(50)
                            .start()
                        
                        val guessNumber = numberPickers.joinToString("") { it.value.toString() }
                        if (guessNumber.length == 3) {
                            attemptsList.add(guessNumber)
                            viewModel.makeGuess(guessNumber.toInt())
                        } else {
                            showErrorDialog("Lütfen 3 basamaklı bir sayı girin")
                        }
                    }
                    .start()
            }
        }
    }

    private fun updateHints(hints: List<Hint>) {
        hintsAdapter.updateHints(hints)
    }

    private fun disableInput() {
        submitButton.isEnabled = false
        numberPickers.forEach { it.isEnabled = false }
    }

    private fun updateScore(score: Int) {
        val oldScore = scoreText.text.toString().replace("Score: ", "").toIntOrNull() ?: 0
        val animator = ValueAnimator.ofInt(oldScore, score)
        animator.duration = 1000
        animator.interpolator = OvershootInterpolator(1.5f)
        
        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            scoreText.text = "Score: $animatedValue"
        }
        
        animator.start()
    }

    private fun startScoreTimer() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while(true) {
                    delay(1000) // Her saniye
                    viewModel.updateScore()
                }
            }
        }
    }

    private fun updateRemainingAttempts(attempts: Int) {
        remainingAttemptsText.apply {
            text = getString(R.string.remaining_attempts, attempts)
            
            val color = when (attempts) {
                3 -> getColor(R.color.colorCorrect)    // Yeşil
                2 -> getColor(R.color.colorMisplaced)  // Turuncu  
                else -> getColor(R.color.colorIncorrect) // Kırmızı
            }
            
            setTextColor(color)
            setShadowLayer(4f, 0f, 2f, Color.parseColor("#40000000"))
            
            if (attempts == 1) {
                val shakeAnimation = AnimationUtils.loadAnimation(context, R.anim.shake_animation)
                startAnimation(shakeAnimation)
            } else {
                alpha = 0f
                scaleX = 0.8f
                scaleY = 0.8f
                animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .setInterpolator(OvershootInterpolator())
                    .start()
            }
        }
    }

    private fun addHint(guess: Int, result: GuessResult) {
        val hint = Hint(
            numbers = listOf(guess),
            description = when (result) {
                GuessResult.Correct -> getString(R.string.hint_correct)
                is GuessResult.Partial -> getString(R.string.hint_partial, result.correctCount, result.misplacedCount)
                GuessResult.Wrong -> getString(R.string.hint_wrong)
            }
        )
        val currentHints = hintsAdapter.getCurrentHints().toMutableList()
        currentHints.add(0, hint)
        hintsAdapter.updateHints(currentHints)
    }

    private fun showErrorDialog(message: String) {
        val dialog = AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("Tamam") { _, _ -> }
            .create()
        dialog.show()
    }

    override fun onPause() {
        soundManager.release()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        soundManager.release()
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setupFullscreen()
        }
    }

    companion object {
        private const val TAG = "GameActivity"
    }
}
