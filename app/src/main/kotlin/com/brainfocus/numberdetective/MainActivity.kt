package com.brainfocus.numberdetective

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.location.LocationManager as AndroidLocationManager
import android.media.AudioAttributes
import android.media.MediaCodec
import android.media.MediaFormat
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.brainfocus.numberdetective.ads.AdManager
import com.brainfocus.numberdetective.auth.GameSignInManager
import com.brainfocus.numberdetective.database.LeaderboardDatabase
import com.brainfocus.numberdetective.databinding.ActivityMainBinding
import com.brainfocus.numberdetective.location.LocationManager
import com.brainfocus.numberdetective.model.GameLocation
import com.brainfocus.numberdetective.ui.leaderboard.LeaderboardFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.games.Games
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
        private const val RC_SIGN_IN = 9001
        private const val RC_LEADERBOARD_UI = 9004
        private const val MAX_STREAMS = 5
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var soundPool: SoundPool
    private lateinit var gameSignInManager: GameSignInManager
    private lateinit var leaderboardDatabase: LeaderboardDatabase
    private lateinit var locationManager: LocationManager
    private var isSignedIn = false
    private var doubleBackToExitPressedOnce = false
    private var quotes: Array<String> = emptyArray()
    private var correctSoundId = 0
    private var wrongSoundId = 0
    private var buttonClickSound = 0
    private var isInitialized = false
    private var _adManager: AdManager? = null
    private val adManager: AdManager
        get() = _adManager ?: AdManager.getInstance(this).also { _adManager = it }

    private val initScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val mediaScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase first
        FirebaseApp.initializeApp(this)
        
        // Initialize sound
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
                
                // Setup click listeners and views after sound is initialized
                setupViews()
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing sound", e)
            }
        }
        
        // Set initial visibility
        binding.quoteContainer.visibility = View.INVISIBLE
        binding.featuresContainer.visibility = View.INVISIBLE
        
        // Load quotes immediately since it's local
        loadQuotes()
        
        lifecycleScope.launch {
            try {
                // Load features
                loadFeatures()
                
                // Initialize ads after content is loaded
                initializeAds()
            } catch (e: Exception) {
                Log.e(TAG, "Error during initialization", e)
            }
        }
    }

    private fun loadQuotes() {
        try {
            // Get quotes from resources
            val quotes = resources.getStringArray(R.array.game_quotes)
            if (quotes.isNotEmpty()) {
                val randomQuote = quotes.random()
                binding.quoteText.text = randomQuote
                binding.quoteText.visibility = View.VISIBLE
                binding.quoteContainer.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading quotes", e)
        }
    }

    private suspend fun loadFeatures() {
        try {
            withContext(Dispatchers.Main) {
                // Set up the feature items
                binding.featuresContainer.apply {
                    visibility = View.VISIBLE
                    
                    // Set up each feature item
                    findViewById<View>(R.id.dikkatFeature)?.apply {
                        findViewById<ImageView>(R.id.featureIcon)?.setImageResource(R.drawable.ic_dikkat)
                        findViewById<TextView>(R.id.featureTitle)?.text = "Dikkat"
                        visibility = View.VISIBLE
                    }
                    
                    findViewById<View>(R.id.hafizaFeature)?.apply {
                        findViewById<ImageView>(R.id.featureIcon)?.setImageResource(R.drawable.ic_hafiza)
                        findViewById<TextView>(R.id.featureTitle)?.text = "Hafıza"
                        visibility = View.VISIBLE
                    }
                    
                    findViewById<View>(R.id.mantikFeature)?.apply {
                        findViewById<ImageView>(R.id.featureIcon)?.setImageResource(R.drawable.ic_mantik)
                        findViewById<TextView>(R.id.featureTitle)?.text = "Mantık"
                        visibility = View.VISIBLE
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading features", e)
        }
    }

    private fun initUI() {
        // Setup click listener
        val clickListener = View.OnClickListener { view ->
            when (view.id) {
                R.id.beyniniKoruButton -> {
                    soundPool?.play(buttonClickSound, 1f, 1f, 1, 0, 1f)
                    startActivity(Intent(this@MainActivity, GameActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    })
                }
                R.id.leaderboardButton -> {
                    soundPool?.play(buttonClickSound, 1f, 1f, 1, 0, 1f)
                    showLeaderboard()
                }
                R.id.settingsButton -> {
                    soundPool?.play(buttonClickSound, 1f, 1f, 1, 0, 1f)
                    /* Handle settings */
                }
            }
        }

        // Initialize views
        binding.apply {
            // Set initial visibility
            listOf(quoteContainer, featuresContainer).forEach { 
                it.visibility = View.INVISIBLE 
            }
            
            // Setup buttons immediately
            listOf(beyniniKoruButton, leaderboardButton, settingsButton).forEach { button ->
                button.apply {
                    visibility = View.VISIBLE
                    setOnClickListener(clickListener)
                }
            }
        }
    }

    private suspend fun initializeSound() = withContext(Dispatchers.Default) {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(MAX_STREAMS)
                .build()

            // Pre-load sounds in parallel
            coroutineScope {
                launch { buttonClickSound = loadSound(R.raw.button_click) }
                launch { correctSoundId = loadSound(R.raw.correct_guess) }
                launch { wrongSoundId = loadSound(R.raw.wrong_guess) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize sound", e)
        }
    }

    private suspend fun loadSound(resId: Int): Int = withContext(Dispatchers.IO) {
        soundPool?.load(applicationContext, resId, 1) ?: 0
    }

    private fun setupViews() = with(binding) {
        val clickListener = View.OnClickListener { view ->
            soundPool?.play(buttonClickSound, 1f, 1f, 1, 0, 1f)
            when (view.id) {
                R.id.beyniniKoruButton -> startActivity(Intent(this@MainActivity, GameActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                })
                R.id.leaderboardButton -> showLeaderboard()
                R.id.settingsButton -> { /* Handle settings */ }
            }
        }

        beyniniKoruButton.setOnClickListener(clickListener)
        leaderboardButton.setOnClickListener(clickListener)
        settingsButton.setOnClickListener(clickListener)
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finish()
                    return
                }

                doubleBackToExitPressedOnce = true
                Toast.makeText(this@MainActivity, "Press BACK again to exit", Toast.LENGTH_SHORT).show()

                Handler(Looper.getMainLooper()).postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2000)
            }
        })
    }

    private fun showLeaderboard() {
        findViewById<TextView>(R.id.titleText).visibility = View.GONE
        findViewById<View>(R.id.brainAnimation).visibility = View.GONE
        findViewById<View>(R.id.quoteContainer).visibility = View.GONE
        findViewById<View>(R.id.featuresContainer).visibility = View.GONE
        findViewById<View>(R.id.descriptionContainer).visibility = View.GONE

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, LeaderboardFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onPause() {
        _adManager?.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        _adManager?.onResume()
    }

    override fun onDestroy() {
        _adManager?.onDestroy()
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setupFullscreen()
        }
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
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    private fun setupMediaCodec() {
        mediaScope.launch {
            try {
                // Configure media codec with recommended buffer size
                val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_MPEG, 44100, 2)
                format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 8192)
                
                // Initialize codec asynchronously
                withContext(Dispatchers.IO) {
                    val codec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_AUDIO_MPEG)
                    codec.configure(format, null, null, 0)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to setup media codec", e)
            }
        }
    }
    
    private fun initializeAds() {
        MobileAds.initialize(this) { 
            val adRequest = AdRequest.Builder().build()
            binding.adView.loadAd(adRequest)
        }
    }
}
