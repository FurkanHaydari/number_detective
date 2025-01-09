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
import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import android.os.Handler
import android.os.Looper

class GameResultActivity : AppCompatActivity() {
    private lateinit var resultText: TextView
    private lateinit var scoreText: TextView
    private lateinit var attemptsText: TextView
    private lateinit var timeText: TextView
    private lateinit var correctAnswerText: TextView
    private lateinit var incorrectGuessesText: TextView
    private lateinit var timeExpiredText: TextView
    private lateinit var backButton: MaterialButton
    private lateinit var leaderboardButton: MaterialButton
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
        const val EXTRA_CORRECT_ANSWER = "CORRECT_ANSWER"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_result)

        setupFullscreen()
        initializeViews()
        setupListeners()
        initializeBannerAd()
        
        val success = intent.getBooleanExtra(EXTRA_SUCCESS, false)
        val score = intent.getIntExtra(EXTRA_SCORE, 0)
        val attempts = intent.getIntExtra(EXTRA_ATTEMPTS, 0)
        val time = intent.getLongExtra(EXTRA_TIME, 0L).toInt()
        
        displayResults(success, score, attempts, time)
        loadInterstitialAd()
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
        resultText = findViewById(R.id.resultText)
        scoreText = findViewById(R.id.scoreText)
        attemptsText = findViewById(R.id.attemptsText)
        timeText = findViewById(R.id.timeText)
        correctAnswerText = findViewById(R.id.correctAnswerText)
        incorrectGuessesText = findViewById(R.id.incorrectGuessesText)

        // Set initial states for animations
        resultText.alpha = 0f
        scoreText.alpha = 0f
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.statsCard).alpha = 0f

        // Initialize buttons with animations
        findViewById<MaterialButton>(R.id.playAgainButton).apply {
            alpha = 0f
            setOnClickListener {
                animateButton(this) {
                    startActivity(Intent(this@GameResultActivity, GameActivity::class.java))
                    finish()
                }
            }
        }
        
        findViewById<MaterialButton>(R.id.leaderboardButton).apply {
            alpha = 0f
            setOnClickListener {
                animateButton(this) {
                    startActivity(Intent(this@GameResultActivity, LeaderboardActivity::class.java))
                }
            }
        }
        
        findViewById<MaterialButton>(R.id.shareButton).apply {
            alpha = 0f
            setOnClickListener {
                animateButton(this) {
                    shareScore()
                }
            }
        }
        
        findViewById<MaterialButton>(R.id.backButton).apply {
            alpha = 0f
            setOnClickListener {
                animateButton(this) {
                    finish()
                }
            }
        }

        // Start animations after a short delay
        handler.postDelayed({
            animateViewsIn()
        }, 300)
    }

    private fun animateButton(button: MaterialButton, action: () -> Unit) {
        button.animate()
            .scaleX(0.92f)
            .scaleY(0.92f)
            .setDuration(100)
            .withEndAction {
                button.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .withEndAction {
                        action.invoke()
                    }
            }
            .start()
    }

    private fun animateViewsIn() {
        // Animate result text
        resultText.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setInterpolator(DecelerateInterpolator())
            .start()

        // Animate score with delay
        handler.postDelayed({
            scoreText.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }, 200)

        // Animate stats card with delay
        handler.postDelayed({
            findViewById<com.google.android.material.card.MaterialCardView>(R.id.statsCard).animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }, 400)

        // Animate buttons
        val buttons = listOf(
            findViewById<MaterialButton>(R.id.playAgainButton),
            findViewById<MaterialButton>(R.id.leaderboardButton),
            findViewById<MaterialButton>(R.id.shareButton),
            findViewById<MaterialButton>(R.id.backButton)
        )

        buttons.forEachIndexed { index, button ->
            handler.postDelayed({
                button.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(400)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }, 600 + (index * 100L))
        }
    }

    private fun setupListeners() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(Intent(this@GameResultActivity, MainActivity::class.java))
                finish()
            }
        })
    }

    private fun displayResults(success: Boolean, score: Int, attempts: Int, time: Int) {
        if (success) {
            resultText.text = "Tebrikler!"
            findViewById<TextView>(R.id.motivationText).visibility = View.GONE
        } else {
            resultText.text = "Tekrar Dene!"
            findViewById<TextView>(R.id.motivationText).visibility = View.VISIBLE
        }
        
        // Animate score number counting up
        val animator = ValueAnimator.ofInt(0, score).apply {
            duration = 1500
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                scoreText.text = animation.animatedValue.toString()
            }
        }
        
        handler.postDelayed({
            animator.start()
        }, 300)
        
        attemptsText.text = attempts.toString()
        timeText.text = "${time}s"

        // Display correct answer with animation
        val correctAnswer = intent.getStringExtra(EXTRA_CORRECT_ANSWER) ?: ""
        correctAnswerText.text = correctAnswer
        correctAnswerText.visibility = View.VISIBLE
        correctAnswerText.alpha = 0f
        correctAnswerText.animate()
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(600)
            .start()

        // Display incorrect guesses with animation
        val guessList = intent.getStringArrayListExtra("attempts_list") ?: arrayListOf()
        if (guessList.isNotEmpty()) {
            incorrectGuessesText.text = guessList.joinToString(", ")
            incorrectGuessesText.visibility = View.VISIBLE
            incorrectGuessesText.alpha = 0f
            incorrectGuessesText.animate()
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(600)
                .start()
        }
        
        // Play sound effect
        val mediaPlayer = MediaPlayer.create(this, if (success) R.raw.victory else R.raw.game_over)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener { it.release() }
    }

    private fun shareScore() {
        val score = intent.getIntExtra(EXTRA_SCORE, 0)
        val shareText = "Skorunuz: $score"

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

    private fun initializeBannerAd() {
        val adView = findViewById<com.google.android.gms.ads.AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private val handler = Handler(Looper.getMainLooper())
}
