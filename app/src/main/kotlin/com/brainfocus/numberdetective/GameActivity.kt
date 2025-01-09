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
import com.brainfocus.numberdetective.model.GuessResult
import com.brainfocus.numberdetective.adapter.HintAdapter
import com.brainfocus.numberdetective.decoration.HintItemDecoration
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.brainfocus.numberdetective.viewmodel.Hint
import android.view.Gravity
import androidx.appcompat.app.AlertDialog
import android.widget.NumberPicker
import android.widget.LinearLayout
import kotlinx.coroutines.flow.combine
import android.graphics.Typeface
import android.content.Intent
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import android.view.WindowManager
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.os.Build
import android.widget.FrameLayout
import android.view.animation.AnimationUtils
import android.graphics.Color
import androidx.core.content.ContextCompat
import android.graphics.Paint
import android.view.animation.OvershootInterpolator
import android.graphics.drawable.ColorDrawable
import android.view.MotionEvent
import android.animation.AnimatorInflater
import android.animation.StateListAnimator
import com.google.android.gms.ads.AdSize
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.graphics.drawable.GradientDrawable
import android.view.HapticFeedbackConstants
import android.widget.EditText
import android.graphics.Rect
import android.graphics.drawable.LayerDrawable
import android.animation.ValueAnimator
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.Spanned
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.sound.SoundManager
import com.brainfocus.numberdetective.ads.AdManager

class GameActivity : AppCompatActivity() {
    private val viewModel: GameViewModel by viewModel()
    private lateinit var hintsContainer: RecyclerView
    private lateinit var hintsAdapter: HintAdapter
    private lateinit var scoreText: TextView
    private lateinit var submitButton: Button
    private val currentNumbers = IntArray(3) { 0 }
    private var numberPickers: List<NumberPicker> = listOf()
    private lateinit var remainingAttemptsText: TextView
    private lateinit var adView: AdView
    private val attemptsList = ArrayList<String>()
    private var pendingGameResult: Intent? = null
    private lateinit var soundManager: SoundManager
    private lateinit var adManager: AdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.activity_game)
        
        setupFullscreen()
        
        adView = findViewById(R.id.adView)
        soundManager = SoundManager(this)
        adManager = AdManager.getInstance(this)
        
        setupViews()
        observeViewModel()
        startScoreTimer()
        loadAds()
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
        setupFullscreen()
        
        hintsContainer = findViewById(R.id.hintsContainer)
        scoreText = findViewById(R.id.scoreText)
        submitButton = findViewById(R.id.submitButton)
        remainingAttemptsText = findViewById(R.id.remainingAttemptsText)
        adView = findViewById(R.id.adView)
        
        setupNumberPickers()
        
        submitButton.setOnClickListener {
            val guess = currentNumbers.joinToString("")
            attemptsList.add(guess)  // Store the guess when it's made
            viewModel.makeGuess(guess.toInt())
        }
        
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

                setFormatter { value -> 
                    SpannableStringBuilder().apply {
                        append(value.toString())
                        setSpan(
                            StyleSpan(Typeface.BOLD),
                            0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        val textColor = if (isDarkMode) Color.WHITE else Color.BLACK
                        setSpan(
                            ForegroundColorSpan(textColor),
                            0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        setSpan(
                            RelativeSizeSpan(1.2f),
                            0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }.toString()
                }
                
                alpha = 0.7f
                
                setOnValueChangedListener { _, oldVal, newVal ->
                    currentNumbers[index] = newVal
                    animateValueChange(this, oldVal, newVal)
                }
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

    private fun animateValueChange(picker: NumberPicker, oldVal: Int, newVal: Int) {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 150
        animator.interpolator = OvershootInterpolator(1.2f)
        
        animator.addUpdateListener { animation ->
            picker.alpha = 0.7f + (animation.animatedValue as Float) * 0.2f
            picker.scaleX = 0.95f + (animation.animatedValue as Float) * 0.05f
            picker.scaleY = 0.95f + (animation.animatedValue as Float) * 0.05f
        }
        
        animator.start()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.hints.combine(viewModel.gameState) { hints, state ->
                    Pair(hints, state)
                }.collect { (hints, state) ->
                    updateHints(hints)
                    
                    handleGameState(state)
                }
            }
        }
    }

    private fun handleGameState(state: GameState) {
        when (state) {
            is GameState.Won -> {
                val intent = Intent(this, GameResultActivity::class.java).apply {
                    putExtra(GameResultActivity.EXTRA_SCORE, state.score)
                    putExtra(GameResultActivity.EXTRA_IS_HIGH_SCORE, false)
                    putExtra(GameResultActivity.EXTRA_IS_WIN, true)
                    putExtra(GameResultActivity.EXTRA_ATTEMPTS, viewModel.getAttempts())
                    putExtra(GameResultActivity.EXTRA_TIME, viewModel.getGameTime().toLong())
                    putExtra(GameResultActivity.EXTRA_CORRECT_ANSWER, viewModel.getCorrectAnswer())
                    putStringArrayListExtra(GameResultActivity.EXTRA_GUESSES, attemptsList)
                }
                showGameResultWithAd(intent)
            }
            is GameState.Lost -> {
                val intent = Intent(this, GameResultActivity::class.java).apply {
                    putExtra(GameResultActivity.EXTRA_SCORE, 0)
                    putExtra(GameResultActivity.EXTRA_IS_HIGH_SCORE, false)
                    putExtra(GameResultActivity.EXTRA_IS_WIN, false)
                    putExtra(GameResultActivity.EXTRA_ATTEMPTS, viewModel.getAttempts())
                    putExtra(GameResultActivity.EXTRA_TIME, viewModel.getGameTime().toLong())
                    putExtra(GameResultActivity.EXTRA_CORRECT_ANSWER, viewModel.getCorrectAnswer())
                    putStringArrayListExtra(GameResultActivity.EXTRA_GUESSES, attemptsList)
                }
                showGameResultWithAd(intent)
            }
            is GameState.Playing -> {
                updateScore(state.score)
                val remainingAttempts = 3 - viewModel.getAttempts()
                updateRemainingAttempts(remainingAttempts)
                if (viewModel.getAttempts() > 0) {
                    showWrongGuessDialog(remainingAttempts)
                }
            }
            is GameState.Error -> {
                showErrorDialog(state.message)
            }
            else -> {
                // Handle other states
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
        animator.duration = 1000 // 1 saniye
        animator.interpolator = OvershootInterpolator(1.5f)
        
        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            scoreText.text = "Score: $animatedValue"
        }
        
        animator.start()
    }

    private fun showGameResultWithAd(intent: Intent) {
        pendingGameResult = intent
        if (adManager.hasLoadedInterstitialAd()) {
            adManager.showInterstitialAd(this)
        } else {
            startActivity(intent)
            finish()
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

    private fun showWrongGuessDialog(remainingAttempts: Int) {
        val message = when (remainingAttempts) {
            2 -> "Yanlış tahmin! 2 hakkınız kaldı."
            1 -> "Yanlış tahmin! Son hakkınız!"
            else -> "Yanlış tahmin!"
        }
        
        AlertDialog.Builder(this)
            .setTitle("Tekrar Deneyin")
            .setMessage(message)
            .setPositiveButton("Tamam", null)
            .show()
    }

    private fun loadAds() {
        adManager.initialize {
            adManager.loadBannerAd(adView)
            adManager.loadInterstitialAd(
                activity = this,
                adUnitId = getString(R.string.interstitial_ad_unit_id),
                onAdLoaded = {},
                onAdDismissed = {
                    pendingGameResult?.let {
                        startActivity(it)
                        finish()
                    }
                },
                onAdFailedToLoad = {
                    pendingGameResult?.let {
                        startActivity(it)
                        finish()
                    }
                },
                onAdFailedToShow = {
                    pendingGameResult?.let {
                        startActivity(it)
                        finish()
                    }
                }
            )
        }
    }

    override fun onPause() {
        adView.pause()
        soundManager.releaseMediaPlayer()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        adView.resume()
    }

    override fun onDestroy() {
        adView.destroy()
        soundManager.releaseMediaPlayer()
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setupFullscreen()
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
        val currentHints = (hintsAdapter.getCurrentHints() + hint).reversed()
        hintsAdapter.updateHints(currentHints)
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
                        
                        val guess = currentNumbers.joinToString("")
                        attemptsList.add(guess)
                        viewModel.makeGuess(guess.toInt())
                    }
                    .start()
            }
        }
    }

    private fun showErrorDialog(message: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_error, null)
        val messageText = dialogView.findViewById<TextView>(R.id.messageText)
        val okButton = dialogView.findViewById<Button>(R.id.okButton)
        
        messageText.text = message
        
        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            attributes?.windowAnimations = R.style.DialogAnimation
        }
        
        okButton.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
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

    companion object {
        private const val TAG = "GameActivity"
        const val EXTRA_ATTEMPTS = "attempts"
        const val EXTRA_TIME = "time"
        const val EXTRA_CORRECT_ANSWER = "correctAnswer"
    }
}
