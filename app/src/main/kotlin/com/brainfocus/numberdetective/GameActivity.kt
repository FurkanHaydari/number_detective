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

class GameActivity : AppCompatActivity() {
    private val viewModel: GameViewModel by viewModel()
    private lateinit var hintsContainer: LinearLayout
    private lateinit var scoreText: TextView
    private lateinit var submitButton: Button
    private val currentNumbers = IntArray(3) { 0 }
    private var numberPickers: List<NumberPicker> = listOf()
    private lateinit var remainingAttemptsText: TextView
    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Önce setContentView çağrılmalı
        setContentView(R.layout.activity_game)
        
        // Sonra tam ekran ayarları yapılmalı
        setupFullscreen()
        
        // Initialize AdView first
        adView = findViewById(R.id.adView)
        
        setupViews()
        setupAds()
        observeViewModel()
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
        // Temel view'ları bul
        hintsContainer = findViewById(R.id.hintsContainer)
        scoreText = findViewById(R.id.scoreText)
        submitButton = findViewById(R.id.submitButton)
        remainingAttemptsText = findViewById(R.id.remainingAttemptsText)
        
        setupNumberPickers()
        setupSubmitButton()
        
        updateRemainingAttempts(3)
    }

    private fun setupNumberPickers() {
        val pickerContainer = findViewById<LinearLayout>(R.id.numberPickerContainer)
        
        // Karanlık mod kontrolü
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

                // Karanlık mod için özel stiller
                if (isDarkMode) {
                    setBackgroundResource(R.drawable.number_picker_background_dark)
                    setDividerColor(ContextCompat.getColor(context, R.color.colorDividerDark))
                } else {
                    setBackgroundResource(R.drawable.number_picker_background)
                    setDividerColor(ContextCompat.getColor(context, R.color.colorDivider))
                }

                // Sayı formatı
                setFormatter { value -> 
                    SpannableStringBuilder().apply {
                        append(value.toString())
                        setSpan(
                            StyleSpan(Typeface.BOLD),
                            0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        // Karanlık modda metin rengi
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
                
                alpha = 0.9f
                
                setOnValueChangedListener { _, oldVal, newVal ->
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
        remainingAttemptsText.apply {
            text = getString(R.string.remaining_attempts, attempts)
            
            // Renk geçişi için renkler
            val color = when (attempts) {
                3 -> getColor(R.color.colorCorrect)    // Yeşil
                2 -> getColor(R.color.colorMisplaced)  // Turuncu  
                else -> getColor(R.color.colorIncorrect) // Kırmızı
            }
            
            setTextColor(color)
            
            // Gölge efekti
            setShadowLayer(4f, 0f, 2f, Color.parseColor("#40000000"))
            
            // Son hak için titreme animasyonu
            if (attempts == 1) {
                val shakeAnimation = AnimationUtils.loadAnimation(context, R.anim.shake_animation)
                startAnimation(shakeAnimation)
            } else {
                // Normal durumlarda pulse animasyonu
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
        showErrorDialog("Maalesef doğru tahminde bulunamadın.\nKalan hakkın: $remainingAttempts")
    }

    private fun setupAds() {
        if (!isAdsInitialized) {
            MobileAds.initialize(this) { initializationStatus ->
                isAdsInitialized = true
                loadAd()
            }
        } else {
            loadAd()
        }
    }

    private fun loadAd() {
        try {
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading ad: ${e.message}")
        }
    }

    override fun onPause() {
        adView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        adView.resume()
    }

    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setupFullscreen()
        }
    }

    private fun addHint(guess: Int, result: GuessResult) {
        val hintView = layoutInflater.inflate(R.layout.hint_item, hintsContainer, false)
        
        // Margin ekle
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, dpToPx(8)) // 8dp bottom margin
        }
        hintView.layoutParams = params
        
        // Gölge ve elevation ekle
        hintView.elevation = dpToPx(4).toFloat()
        hintView.setBackgroundResource(R.drawable.hint_card_background)
        
        hintView.findViewById<TextView>(R.id.guessNumberText).text = guess.toString()
        
        val hintText = hintView.findViewById<TextView>(R.id.hintText)
        val hintIcon = hintView.findViewById<ImageView>(R.id.hintIcon)
        
        when (result) {
            GuessResult.Correct -> {
                hintText.text = getString(R.string.hint_correct)
                hintIcon.setImageResource(R.drawable.ic_correct)
                hintView.setBackgroundResource(R.drawable.hint_box_correct_background)
            }
            is GuessResult.Partial -> {
                hintText.text = getString(R.string.hint_partial, 
                    result.correctCount, 
                    result.misplacedCount)
                hintIcon.setImageResource(R.drawable.ic_partial)
                hintView.setBackgroundResource(R.drawable.hint_box_partial_background)
            }
            GuessResult.Wrong -> {
                hintText.text = getString(R.string.hint_wrong)
                hintIcon.setImageResource(R.drawable.ic_wrong)
                hintView.setBackgroundResource(R.drawable.hint_box_wrong_background)
            }
        }
        
        // Animasyonlu ekleme
        hintView.alpha = 0f
        hintView.translationX = -100f
        
        hintsContainer.addView(hintView, 0)
        
        hintView.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(300)
            .setInterpolator(OvershootInterpolator())
            .start()
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
            
            // Hover efekti
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
                        
                        val guess = currentNumbers.joinToString("").toInt()
                        viewModel.makeGuess(guess)
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

    companion object {
        private const val TAG = "GameActivity"
        private var isAdsInitialized = false
    }
}
