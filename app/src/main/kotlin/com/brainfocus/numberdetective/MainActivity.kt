package com.brainfocus.numberdetective

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
import com.brainfocus.numberdetective.location.LocationManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.games.Games
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var locationManager: LocationManager

    @Inject
    lateinit var signInManager: GameSignInManager

    @Inject
    lateinit var adManager: AdManager

    private lateinit var binding: ActivityMainBinding
    private var soundPool: SoundPool? = null
    private var buttonClickSound = 0
    private var isSignedIn = false
    private var doubleBackToExitPressedOnce = false

    private val leaderboardLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // Leaderboard was closed
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val LEADERBOARD_ID = "CgkIxZWJ8KYWEAIQAQ"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                signInManager.initialize(this@MainActivity)
                adManager.initialize()

                // Load test ad
                val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
                binding.adView.loadAd(adRequest)

                // Setup views and animations
                setupViews()
                setupAnimations()
                setupBackPressHandler()

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
                binding.quoteText.text = randomQuote
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
                    findViewById<TextView>(R.id.featureTitle).text = "Dikkat"
                    visibility = View.VISIBLE
                }

                binding.hafizaFeature.root.apply {
                    findViewById<ImageView>(R.id.featureIcon).setImageResource(R.drawable.ic_hafiza)
                    findViewById<TextView>(R.id.featureTitle).text = "Hafıza"
                    visibility = View.VISIBLE
                }

                binding.mantikFeature.root.apply {
                    findViewById<ImageView>(R.id.featureIcon).setImageResource(R.drawable.ic_mantik)
                    findViewById<TextView>(R.id.featureTitle).text = "Mantık"
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

    private fun setupViews() {
        binding.apply {
            // Setup start game button
            startGameButton.setOnClickListener {
                soundPool?.play(buttonClickSound, 1f, 1f, 1, 0, 1f)
                startActivity(Intent(this@MainActivity, GameActivity::class.java))
            }

            // Setup description text
            descriptionText.apply {
                visibility = View.VISIBLE
                text = getString(R.string.game_description)
                setTextColor(getColor(R.color.white))
                alpha = 0.9f
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
}
