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
        
        // Tam ekran modu için
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        setContentView(R.layout.activity_game)
        
        // Gezinme çubuğunu ve sistem UI'ı gizle
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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
        
        // Initialize AdMob
        MobileAds.initialize(this)
        
        setupViews()
        setupAds()
        observeViewModel()
    }

    private fun setupViews() {
        // Temel view'ları bul
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

    private fun setupAds() {
        MobileAds.initialize(this) {
            // Initialization completed
        }
        
        adView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        
        adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.d("Ads", "Error loading ad: ${error.message}")
            }
            
            override fun onAdLoaded() {
                Log.d("Ads", "Ad loaded successfully")
            }
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
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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
}
