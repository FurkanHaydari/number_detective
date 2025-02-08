package com.brainfocus.numberdetective

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.brainfocus.numberdetective.ads.AdManager
import com.brainfocus.numberdetective.auth.GameSignInManager
import com.brainfocus.numberdetective.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.games.Games
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.brainfocus.numberdetective.utils.LocaleHelper
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var signInManager: GameSignInManager
    
    @Inject
    lateinit var adManager: AdManager

    private lateinit var binding: ActivityMainBinding
    private var soundPool: SoundPool? = null
    private var buttonClickSound = 0
    private var isSignedIn = false
    private var doubleBackToExitPressedOnce = false

    companion object {
        private const val TAG = "MainActivity"
    }




    private fun handleSignInResult(result: GameSignInManager.SignInResult) {
        when (result) {
            is GameSignInManager.SignInResult.Success -> {
                isSignedIn = true
                Log.d(TAG, "Sign in success")
                // The welcome popup will be shown automatically by the SDK
            }
            is GameSignInManager.SignInResult.Cancelled -> {
                Log.d(TAG, "Sign in cancelled")
                // Try sign-in again since it's required
                startSignIn()
            }
            is GameSignInManager.SignInResult.Error -> {
                Log.e(TAG, "Sign in error", result.exception)
                Toast.makeText(this, "Sign in failed: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                // Try sign-in again since it's required
                startSignIn()
            }
        }
    }

    private fun startSignIn() {
        lifecycleScope.launch {
            val result = signInManager.signIn(this@MainActivity)
            handleSignInResult(result)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Try silent sign-in on startup
        lifecycleScope.launch {
            val result = signInManager.signInSilently(this@MainActivity)
            handleSignInResult(result)
        }
        setupFullscreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set initial visibility
        binding.quoteContainer.visibility = View.INVISIBLE
        binding.featuresContainer.visibility = View.INVISIBLE

        lifecycleScope.launch {
            try {
                // Initialize sound pool
                soundPool = SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .build()

                // Load button click sound
                buttonClickSound = loadSound(R.raw.button_click)

                // Initialize components
                adManager.initialize()
                
                // Remove location permission check and directly check Play Games sign in
                checkPlayGamesSignIn()

                // Load test ad
                val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
                binding.adView.loadAd(adRequest)

                // Setup views and animations
                setupViews()
                setupAnimations()
                setupBackPressHandler()
                setupLanguageButtons()

                // Load and show content
                loadQuotes()
                loadFeatures()
            } catch (e: Exception) {
                Log.e(TAG, "Error during initialization", e)
            }
        }
    }

    private suspend fun loadSound(resId: Int): Int = withContext(Dispatchers.IO) {
        soundPool?.load(applicationContext, resId, 1) ?: 0
    }

    private fun loadQuotes() {
        try {
            val quotes = resources.getStringArray(R.array.game_quotes)
            if (quotes.isNotEmpty()) {
                val randomQuote = quotes.random()
                val parts = randomQuote.split(" - ")
                if (parts.size == 2) {
                    binding.quoteText.text = parts[0].trim()
                    binding.quoteAuthor.text = "\n" + "-" + parts[1].trim()
                }
                binding.quoteContainer.apply {
                    visibility = View.VISIBLE
                    alpha = 0f
                    animate()
                        .alpha(1f)
                        .setDuration(800)
                        .start()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading quotes", e)
        }
    }

    private fun loadFeatures() {
        try {
            binding.featuresContainer.apply {
                visibility = View.VISIBLE
                alpha = 0f

                // Set up each feature item
                binding.dikkatFeature.root.apply {
                    findViewById<ImageView>(R.id.featureIcon).setImageResource(R.drawable.ic_dikkat)
                    findViewById<TextView>(R.id.featureTitle).text = getString(R.string.feature_attention)
                    visibility = View.VISIBLE
                }

                binding.hafizaFeature.root.apply {
                    findViewById<ImageView>(R.id.featureIcon).setImageResource(R.drawable.ic_hafiza)
                    findViewById<TextView>(R.id.featureTitle).text = getString(R.string.feature_memory)
                    visibility = View.VISIBLE
                }

                binding.mantikFeature.root.apply {
                    findViewById<ImageView>(R.id.featureIcon).setImageResource(R.drawable.ic_mantik)
                    findViewById<TextView>(R.id.featureTitle).text = getString(R.string.feature_logic)
                    visibility = View.VISIBLE
                }

                animate()
                    .alpha(1f)
                    .setDuration(800)
                    .setStartDelay(400)
                    .start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading features", e)
        }
    }

    private fun checkPlayGamesSignIn() {
        lifecycleScope.launch {
            try {
                when (val result = signInManager.signInSilently(this@MainActivity)) {
                    is GameSignInManager.SignInResult.Success -> {
                        isSignedIn = true
                        setupViews()
                    }
                    is GameSignInManager.SignInResult.Cancelled -> {
                        val signInResult = signInManager.signIn(this@MainActivity)
                        when (signInResult) {
                            is GameSignInManager.SignInResult.Success -> {
                                isSignedIn = true
                                setupViews()
                            }
                            else -> {
                                Toast.makeText(this@MainActivity, getString(R.string.error_play_games_required), Toast.LENGTH_LONG).show()
                                binding.startGameButton.isEnabled = false
                            }
                        }
                    }
                    is GameSignInManager.SignInResult.Error -> {
                        Toast.makeText(this@MainActivity, getString(R.string.error_sign_in, result.exception.message), Toast.LENGTH_LONG).show()
                        binding.startGameButton.isEnabled = false
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sign-in error", e)
                Toast.makeText(this@MainActivity, getString(R.string.error_sign_in, e.message), Toast.LENGTH_LONG).show()
                binding.startGameButton.isEnabled = false
            }
        }
    }

    private fun setupViews() {
        binding.apply {
            // Setup start game button
            startGameButton.setOnClickListener {
                if (!isSignedIn) {
                    Toast.makeText(this@MainActivity, getString(R.string.error_play_games_sign_in_first), Toast.LENGTH_SHORT).show()
                    checkPlayGamesSignIn()
                    return@setOnClickListener
                }
                soundPool?.play(buttonClickSound, 1f, 1f, 1, 0, 1f)
                startActivity(Intent(this@MainActivity, GameActivity::class.java))
            }
        }
    }

    private fun setupAnimations() {
        binding.apply {
            startGameButton.apply {
                alpha = 0f
                translationY = 50f
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(1000)
                    .setStartDelay(500)
                    .start()
            }
        }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    isEnabled = false
                    finishAffinity()
                } else {
                    doubleBackToExitPressedOnce = true
                    Toast.makeText(this@MainActivity, getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show()

                    Handler(Looper.getMainLooper()).postDelayed({
                        doubleBackToExitPressedOnce = false
                    }, 2000)
                }
            }
        })
    }

    private fun setupFullscreen() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    override fun onResume() {
        super.onResume()
        binding.startGameButton.isEnabled = true
        adManager.onResume()
    }

    override fun onPause() {
        super.onPause()
        adManager.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool?.release()
        soundPool = null
        adManager.release()
    }

    private fun setupLanguageButtons() {
        binding.turkishButton.setOnClickListener {
            setLocale("tr")
        }

        binding.englishButton.setOnClickListener {
            setLocale("en")
        }

        // Set initial button states
        updateLanguageButtonStates(LocaleHelper.getLanguage(this))
    }

    private fun setLocale(languageCode: String) {
        LocaleHelper.setLocale(this, languageCode)
        updateLanguageButtonStates(languageCode)
        recreate()
    }

    private fun updateLanguageButtonStates(selectedLanguage: String) {
        binding.turkishButton.alpha = if (selectedLanguage == "tr") 1.0f else 0.5f
        binding.englishButton.alpha = if (selectedLanguage == "en") 1.0f else 0.5f
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }
}
