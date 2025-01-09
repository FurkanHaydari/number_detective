package com.brainfocus.numberdetective

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.brainfocus.numberdetective.auth.GameSignInManager
import com.brainfocus.numberdetective.repository.LeaderboardRepository
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import android.util.Log
import android.view.WindowInsetsController
import android.os.Build
import android.widget.Toast

class GameResultActivity : AppCompatActivity() {
    private lateinit var resultAnimation: LottieAnimationView
    private lateinit var scoreText: TextView
    private lateinit var attemptsText: TextView
    private lateinit var timeText: TextView
    private lateinit var resultText: TextView
    private lateinit var shareButton: MaterialButton
    private lateinit var playAgainButton: MaterialButton
    private var mInterstitialAd: InterstitialAd? = null
    private var mediaPlayer: MediaPlayer? = null
    private val TAG = "GameResultActivity"

    companion object {
        const val EXTRA_SCORE = "SCORE"
        const val EXTRA_ATTEMPTS = "ATTEMPTS"
        const val EXTRA_TIME = "TIME"
        const val EXTRA_SUCCESS = "SUCCESS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_result)

        setupFullscreen()
        initializeViews()
        setupListeners()
        displayResults()
        setupButtons()
        loadInterstitialAd()
        
        val success = intent.getBooleanExtra(EXTRA_SUCCESS, false)
        val score = intent.getIntExtra(EXTRA_SCORE, 0)
        val attempts = intent.getIntExtra(EXTRA_ATTEMPTS, 0)
        val time = intent.getIntExtra(EXTRA_TIME, 0)
        
        displayResults(success, score, attempts, time)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun setupFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun initializeViews() {
        resultAnimation = findViewById(R.id.resultAnimation)
        resultText = findViewById(R.id.resultText)
        scoreText = findViewById(R.id.scoreText)
        attemptsText = findViewById(R.id.attemptsText)
        timeText = findViewById(R.id.timeText)
        shareButton = findViewById(R.id.shareButton)
        playAgainButton = findViewById(R.id.playAgainButton)
        
        // Initialize animation view
        resultAnimation.apply {
            scaleX = 0.8f
            scaleY = 0.8f
            repeatCount = LottieDrawable.INFINITE
        }
    }

    private fun setupListeners() {
        shareButton.setOnClickListener {
            shareScore()
        }

        playAgainButton.setOnClickListener {
            showInterstitialAd()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(Intent(this@GameResultActivity, MainActivity::class.java))
                finish()
            }
        })
    }

    private fun displayResults() {
        val score = intent.getIntExtra(EXTRA_SCORE, 0)
        val attempts = intent.getIntExtra(EXTRA_ATTEMPTS, 0)
        val time = intent.getIntExtra(EXTRA_TIME, 0)
        val success = intent.getBooleanExtra(EXTRA_SUCCESS, false)

        scoreText.text = getString(R.string.score_text, score)
        attemptsText.text = getString(R.string.attempts_text, attempts)
        timeText.text = getString(R.string.time_text, time)

        showResult(success)
    }

    private fun displayResults(success: Boolean, score: Int, attempts: Int, time: Int) {
        scoreText.text = getString(R.string.score_text, score)
        attemptsText.text = getString(R.string.attempts_text, attempts)
        timeText.text = getString(R.string.time_text, time)

        showResult(success)
    }

    private fun showResult(success: Boolean) {
        if (success) {
            resultText.text = getString(R.string.result_congrats)
            resultAnimation.apply {
                setAnimation(R.raw.win_animation)
                playAnimation()
            }
            playSound(true)
        } else {
            resultText.text = getString(R.string.result_try_again)
            resultAnimation.apply {
                setAnimation(R.raw.lose_animation)
                playAnimation()
            }
            playSound(false)
        }
    }

    private fun shareScore() {
        val score = intent.getIntExtra(EXTRA_SCORE, 0)
        val shareText = getString(R.string.share_score_text, score)

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        startActivity(Intent.createChooser(shareIntent, null))
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            this,
            getString(R.string.interstitial_ad_unit_id),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    setupInterstitialCallbacks()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d("Ads", loadAdError.message)
                    mInterstitialAd = null
                }
            }
        )
    }

    private fun setupInterstitialCallbacks() {
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d("Ads", "Ad was dismissed.")
                mInterstitialAd = null
                startActivity(Intent(this@GameResultActivity, GameActivity::class.java))
                finish()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e("Ads", "Ad failed to show.")
                mInterstitialAd = null
                startActivity(Intent(this@GameResultActivity, GameActivity::class.java))
                finish()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d("Ads", "Ad showed fullscreen content.")
            }
        }
    }

    private fun showInterstitialAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
        } else {
            Log.d("Ads", "The interstitial ad wasn't ready yet.")
            startActivity(Intent(this, GameActivity::class.java))
            finish()
        }
    }

    private fun setupButtons() {
        val buttonPlayAgain = findViewById<Button>(R.id.playAgainButton)
        val buttonHome = findViewById<Button>(R.id.homeButton)
        val buttonLeaderboard = findViewById<Button>(R.id.leaderboardButton)

        buttonPlayAgain.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
            finish()
        }

        buttonHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        buttonLeaderboard.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }

        updateLeaderboardScore()
    }

    private fun updateLeaderboardScore() {
        val score = intent.getIntExtra(EXTRA_SCORE, 0)
        val signInManager = GameSignInManager(this)
        signInManager.initializeSignIn()
        signInManager.signIn(
            onSuccess = { account ->
                val repository = LeaderboardRepository(this)
                lifecycleScope.launch {
                    try {
                        repository.updatePlayerScore(account, score)
                    } catch (e: Exception) {
                        Log.e("GameResultActivity", "Error updating score: ${e.message}")
                    }
                }
            },
            onFailed = {
                Log.e("GameResultActivity", "Sign in failed, score not updated")
            }
        )
    }

    private fun playSound(isWin: Boolean) {
        try {
            val soundResId = if (isWin) R.raw.victory else R.raw.game_over
            val mediaPlayer = MediaPlayer.create(this, soundResId)
            mediaPlayer?.apply {
                setOnCompletionListener { release() }
                start()
            } ?: run {
                Log.e(TAG, "Failed to create MediaPlayer")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound: ${e.message}")
        }
    }
}
