package com.brainfocus.numberdetective

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.material.button.MaterialButton
import androidx.activity.OnBackPressedCallback
import android.util.Log
import android.widget.Button
import android.app.ActivityOptions
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.os.Build
import android.widget.Toast

class GameResultActivity : AppCompatActivity() {
    private lateinit var resultAnimation: LottieAnimationView
    private lateinit var resultText: TextView
    private lateinit var scoreText: TextView
    private lateinit var attemptsText: TextView
    private lateinit var timeText: TextView
    private lateinit var bestScoreText: TextView
    private lateinit var shareButton: Button
    private lateinit var playAgainButton: Button
    private lateinit var correctAnswerText: TextView
    private lateinit var attemptsListText: TextView
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Önce tam ekran ayarları yapılmalı
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        // Sonra setContentView çağrılmalı
        setContentView(R.layout.activity_game_result)
        
        setupFullscreen()
        
        // Initialize MobileAds before loading the ad
        MobileAds.initialize(this) {
            loadInterstitialAd()
        }
        
        initializeViews()
        setupListeners()
        displayResults()
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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setupFullscreen()
        }
    }

    private fun initializeViews() {
        resultAnimation = findViewById(R.id.resultAnimation)
        resultText = findViewById(R.id.resultText)
        scoreText = findViewById(R.id.scoreText)
        attemptsText = findViewById(R.id.attemptsText)
        timeText = findViewById(R.id.timeText)
        bestScoreText = findViewById(R.id.bestScoreText)
        shareButton = findViewById(R.id.shareButton)
        playAgainButton = findViewById(R.id.playAgainButton)
        correctAnswerText = findViewById(R.id.correctAnswerText)
        attemptsListText = findViewById(R.id.attemptsListText)
    }

    private fun setupListeners() {
        shareButton.setOnClickListener {
            shareScore()
        }

        playAgainButton.setOnClickListener {
            startMainActivity()
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    startMainActivity()
                }
            }
        )
    }

    private fun startMainActivity() {
        try {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            
            // Geçiş animasyonunu kaldır
            finishAfterTransition()
        } catch (e: Exception) {
            Log.e("GameResultActivity", "Error starting MainActivity", e)
            finishAffinity()
        }
    }

    private fun displayResults() {
        val isWin = intent.getBooleanExtra("is_win", false)
        val score = intent.getIntExtra("score", 0)
        val correctAnswer = intent.getStringExtra("correct_answer") ?: ""
        val attemptsList = intent.getStringArrayListExtra("attempts_list") ?: arrayListOf()
        
        // Doğru cevabı göster
        correctAnswerText.text = "Doğru Cevap: $correctAnswer"
        
        // Denemeleri göster
        val attemptsBuilder = StringBuilder()
        attemptsList.forEachIndexed { index, attempt ->
            attemptsBuilder.append("${index + 1}. Deneme: $attempt\n")
        }
        attemptsListText.text = attemptsBuilder.toString()
        
        // Animasyon ve metin güncelleme
        if (isWin) {
            resultAnimation.setAnimation(R.raw.win_animation)
            resultText.text = getString(R.string.result_congrats)
        } else {
            resultAnimation.setAnimation(R.raw.lose_animation)
            resultText.text = getString(R.string.result_try_again)
        }
        
        resultAnimation.playAnimation()
        scoreText.text = getString(R.string.score_text, score)
        
        // Diğer sonuç bilgilerini göster
        val attempts = intent.getIntExtra("attempts", 0)
        val timeSeconds = intent.getLongExtra("time_seconds", 0)
        
        attemptsText.text = getString(R.string.attempts_text, attempts)
        timeText.text = getString(R.string.time_text, timeSeconds)
    }

    private fun shareScore() {
        val score = intent.getIntExtra("SCORE", 0)
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_score_text, score))
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Skoru Paylaş"))
    }

    override fun onPause() {
        super.onPause()
        resultAnimation.pauseAnimation()
    }

    override fun onDestroy() {
        resultAnimation.cancelAnimation()
        super.onDestroy()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startMainActivity()
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder()
            .build()
        
        InterstitialAd.load(this, getString(R.string.interstitial_ad_unit_id), adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d("Ads", "Interstitial ad loaded successfully")
                    mInterstitialAd = interstitialAd
                    setupInterstitialCallbacks()
                    // Ensure the activity is in immersive mode before showing the ad
                    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                    showInterstitialAd()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e("Ads", "Interstitial ad failed to load: ${loadAdError.message}")
                    mInterstitialAd = null
                }
            }
        )
    }

    private fun setupInterstitialCallbacks() {
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d("Ads", "Interstitial ad was dismissed")
                mInterstitialAd = null
                // Restore immersive mode after ad is dismissed
                setupFullscreen()
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                Log.e("Ads", "Interstitial ad failed to show: ${adError.message}")
                mInterstitialAd = null
                setupFullscreen()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d("Ads", "Interstitial ad showed fullscreen content")
            }

            override fun onAdImpression() {
                Log.d("Ads", "Interstitial ad recorded an impression")
            }

            override fun onAdClicked() {
                Log.d("Ads", "Interstitial ad was clicked")
            }
        }
    }

    private fun showInterstitialAd() {
        if (mInterstitialAd != null) {
            // Ensure we're in immersive mode
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            mInterstitialAd?.show(this)
        } else {
            Log.d("Ads", "The interstitial ad wasn't ready yet.")
        }
    }
}
