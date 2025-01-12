package com.brainfocus.numberdetective

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
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
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.brainfocus.numberdetective.ads.AdManager
import com.brainfocus.numberdetective.auth.GameSignInManager
import com.brainfocus.numberdetective.database.LeaderboardDatabase
import com.brainfocus.numberdetective.location.LocationManager
import com.brainfocus.numberdetective.model.GameLocation
import com.brainfocus.numberdetective.ui.leaderboard.LeaderboardFragment
import com.google.android.gms.ads.AdView
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.games.Games
import com.google.android.gms.location.LocationRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "MainActivity"
    }

    private lateinit var adView: AdView
    private lateinit var adManager: AdManager
    private lateinit var descriptionText: TextView
    private lateinit var locationManager: LocationManager
    private lateinit var oneTapClient: SignInClient
    private var isSignedIn = false
    private var doubleBackToExitPressedOnce = false

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                showLeaderboard()
            }
            else -> {
                showLeaderboard()
            }
        }
    }

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val credential = Identity.getSignInClient(this).getSignInCredentialFromIntent(result.data)
                isSignedIn = true
                handleSignInSuccess(credential)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get credential: ${e.localizedMessage}")
                showNameInputDialog()
            }
        } else {
            showNameInputDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupFullscreen()
        
        // Initialize views and managers
        adView = findViewById(R.id.adView)
        adManager = AdManager.getInstance(this)
        descriptionText = findViewById(R.id.descriptionText)
        locationManager = LocationManager()

        // Setup essential features immediately
        setupGoogleSignIn()
        setupViews()
        
        // Launch non-blocking operations
        lifecycleScope.launch(Dispatchers.IO) {
            loadAds()
            checkSignIn()
        }
        
        // Setup UI elements with slight delays to improve performance
        lifecycleScope.launch {
            withContext(Dispatchers.Default) {
                // Prepare data in background
                val buttonsData = prepareButtonsData()
                val featuresData = prepareFeaturesData()
                val animationData = prepareAnimationData()
                val (quoteText, authorText) = prepareQuoteData()
                val descriptionData = prepareDescriptionData()

                // Switch to main thread for UI updates
                withContext(Dispatchers.Main) {
                    setupButtons()
                    setupFeatures()
                    delay(100)
                    setupAnimation()
                    setupQuoteOfDay(quoteText, authorText)
                    setupDescription()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finish()
                    return
                }

                doubleBackToExitPressedOnce = true
                Toast.makeText(this@MainActivity, getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show()

                Handler(Looper.getMainLooper()).postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2000)
            }
        })
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

    private fun loadAds() {
        adManager.initialize {
            adManager.loadBannerAd(adView)
        }
    }

    private fun setupGoogleSignIn() {
        try {
            oneTapClient = Identity.getSignInClient(this)
            if (BuildConfig.WEB_CLIENT_ID.isNotEmpty()) {
                val signInRequest = BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                            .setFilterByAuthorizedAccounts(true)
                            .build()
                    )
                    .setAutoSelectEnabled(true)
                    .build()

                oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener { result ->
                        try {
                            @Suppress("DEPRECATION")
                            startIntentSenderForResult(
                                result.pendingIntent.intentSender, RC_SIGN_IN,
                                null, 0, 0, 0, null)
                        } catch (e: IntentSender.SendIntentException) {
                            Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                            proceedWithoutSignIn()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, e.localizedMessage ?: "Unknown error during sign in")
                        proceedWithoutSignIn()
                    }
            } else {
                Log.w(TAG, "Google Sign-In is not configured. Proceeding without sign-in.")
                proceedWithoutSignIn()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up Google Sign-In: ${e.localizedMessage}")
            proceedWithoutSignIn()
        }
    }

    private fun checkSignIn() {
        try {
            oneTapClient = Identity.getSignInClient(this)
            if (BuildConfig.WEB_CLIENT_ID.isNotEmpty()) {
                val signInRequest = BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                            .setFilterByAuthorizedAccounts(true)
                            .build()
                    )
                    .setAutoSelectEnabled(true)
                    .build()

                oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener { result ->
                        try {
                            @Suppress("DEPRECATION")
                            startIntentSenderForResult(
                                result.pendingIntent.intentSender, RC_SIGN_IN,
                                null, 0, 0, 0, null)
                        } catch (e: IntentSender.SendIntentException) {
                            Log.e(TAG, "Silent sign-in failed: ${e.localizedMessage}")
                            proceedWithoutSignIn()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Silent sign-in failed: ${e.localizedMessage}")
                        proceedWithoutSignIn()
                    }
            } else {
                Log.w(TAG, "Google Sign-In is not configured. Proceeding without sign-in.")
                proceedWithoutSignIn()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking sign-in: ${e.localizedMessage}")
            proceedWithoutSignIn()
        }
    }

    private fun signInToPlayGames() {
        try {
            oneTapClient = Identity.getSignInClient(this)
            if (BuildConfig.WEB_CLIENT_ID.isNotEmpty()) {
                val signInRequest = BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                            .setFilterByAuthorizedAccounts(false)
                            .build()
                    )
                    .setAutoSelectEnabled(false)
                    .build()

                oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener { result ->
                        try {
                            @Suppress("DEPRECATION")
                            startIntentSenderForResult(
                                result.pendingIntent.intentSender, RC_SIGN_IN,
                                null, 0, 0, 0, null)
                        } catch (e: IntentSender.SendIntentException) {
                            Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                            proceedWithoutSignIn()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Sign-in failed: ${e.localizedMessage ?: "Unknown error"}")
                        proceedWithoutSignIn()
                    }
            } else {
                Log.w(TAG, "Google Sign-In is not configured. Proceeding without sign-in.")
                proceedWithoutSignIn()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during sign-in: ${e.localizedMessage}")
            proceedWithoutSignIn()
        }
    }

    private fun proceedWithoutSignIn() {
        isSignedIn = false
        showNameInputDialog()
    }

    private fun handleSignInSuccess(credential: SignInCredential) {
        @Suppress("UNUSED_VARIABLE") val userId = credential.id
        
        lifecycleScope.launch {
            // leaderboardDatabase.updatePlayerScore(
            //     userId = userId,
            //     score = 0,
            //     location = GameLocation()
            // )
        }
    }

    private fun showNameInputDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_name_input, null)
        val nameInput = view.findViewById<TextInputEditText>(R.id.nameInput)

        MaterialAlertDialogBuilder(this)
            .setTitle("İsminizi Girin")
            .setView(view)
            .setCancelable(false)
            .setPositiveButton("Tamam") { _, _ ->
                val displayName = nameInput.text.toString()
                if (displayName.isNotEmpty()) {
                    lifecycleScope.launch {
                        // leaderboardDatabase.updatePlayerScore(
                        //     userId = System.currentTimeMillis().toString(),
                        //     score = 0,
                        //     location = GameLocation()
                        // )
                    }
                }
            }
            .show()
    }

    private fun setupViews() {
        val beyniniKoruButton = findViewById<MaterialButton>(R.id.beyniniKoruButton)
        beyniniKoruButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

        val leaderboardButton = findViewById<MaterialButton>(R.id.leaderboardButton)
        leaderboardButton.setOnClickListener {
            if (isSignedIn) {
                checkLocationPermission()
            } else {
                signInToPlayGames()
            }
        }

        val startButton = findViewById<MaterialButton>(R.id.beyniniKoruButton)
        startButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupAnimation() {
        val dikkatFeature = findViewById<ViewGroup>(R.id.dikkatFeature)
        val hafizaFeature = findViewById<ViewGroup>(R.id.hafizaFeature)
        val mantikFeature = findViewById<ViewGroup>(R.id.mantikFeature)

        val pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_animation)
        
        dikkatFeature.startAnimation(pulseAnimation)
        hafizaFeature.startAnimation(pulseAnimation)
        mantikFeature.startAnimation(pulseAnimation)

        val startButton = findViewById<MaterialButton>(R.id.beyniniKoruButton)
        val buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.glow_animation)
        
        startButton.post {
            val parent = startButton.parent as ViewGroup
            val buttonIndex = parent.indexOfChild(startButton)
            
            val frameLayout = FrameLayout(this).apply {
                val buttonWidth = startButton.measuredWidth
                val buttonHeight = startButton.measuredHeight
                
                layoutParams = ConstraintLayout.LayoutParams(
                    buttonWidth,
                    buttonHeight
                ).apply {
                    bottomToTop = R.id.adContainer
                    leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
                    rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
                    bottomMargin = resources.getDimensionPixelSize(R.dimen.button_margin_bottom)
                }
                clipChildren = true
                clipToOutline = true
            }
            
            parent.removeView(startButton)
            frameLayout.addView(startButton, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ))
            
            val overlayView = View(this).apply {
                val overlayWidth = (startButton.measuredWidth * 0.2).toInt()
                val overlayHeight = startButton.measuredHeight - 16
                
                layoutParams = FrameLayout.LayoutParams(
                    overlayWidth,
                    overlayHeight
                ).apply {
                    gravity = android.view.Gravity.CENTER
                    setMargins(8, 8, 8, 8)
                }
                background = AppCompatResources.getDrawable(context, R.drawable.button_glow_overlay)
                elevation = startButton.elevation - 1f
            }
            
            frameLayout.addView(overlayView)
            parent.addView(frameLayout, buttonIndex)
            overlayView.startAnimation(buttonAnimation)
        }
    }

    private fun setupButtons() {
        findViewById<MaterialButton>(R.id.beyniniKoruButton).apply {
            setOnClickListener {
                val intent = Intent(this@MainActivity, GameActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }

        findViewById<MaterialButton>(R.id.leaderboardButton).apply {
            visibility = View.VISIBLE
            setOnClickListener {
                showLeaderboard()
            }
        }

        findViewById<MaterialButton>(R.id.settingsButton).apply {
            visibility = View.VISIBLE
            setOnClickListener {
                // Handle settings click
            }
        }
    }

    private fun setupFeatures() {
        data class Feature(val viewId: Int, val iconRes: Int, val title: String)
        
        val features = listOf(
            Feature(R.id.dikkatFeature, R.drawable.dikkat, "Dikkat"),
            Feature(R.id.hafizaFeature, R.drawable.hafiza, "Hafıza"),
            Feature(R.id.mantikFeature, R.drawable.mantik, "Mantık")
        )
        
        features.forEach { feature ->
            val featureView = findViewById<ViewGroup>(feature.viewId)
            featureView.findViewById<ImageView>(R.id.featureIcon).setImageResource(feature.iconRes)
            featureView.findViewById<TextView>(R.id.featureTitle).text = feature.title
        }

        val titleText = findViewById<TextView>(R.id.titleText)
        val quoteText = findViewById<TextView>(R.id.quoteText)
        
        titleText.apply {
            setTextColor(getColor(R.color.titleTextColor))
            setShadowLayer(4f, 0f, 2f, Color.parseColor("#40000000"))
        }
        
        quoteText.apply {
            val quoteSpannable = SpannableStringBuilder().apply {
                append("❝   ", StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(
                    RelativeSizeSpan(1.4f),
                    0,
                    1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpan(
                    ForegroundColorSpan(Color.parseColor("#FFE500")),
                    0,
                    1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                
                append(text)
            }
            
            text = quoteSpannable
            setTextColor(getColor(R.color.quoteTextColor))
            setShadowLayer(4f, 0f, 2f, Color.parseColor("#40000000"))
        }
    }

    private fun prepareQuoteData(): Pair<String, String> {
        val quotes = resources.getStringArray(R.array.game_quotes)
        val randomQuote = quotes.random()
        val parts = randomQuote.split(" - ")
        return Pair(parts[0], "- ${parts[1]}")
    }

    private fun setupQuoteOfDay(quoteText: String, authorText: String) {
        findViewById<TextView>(R.id.quoteText).text = quoteText
        findViewById<TextView>(R.id.quoteAuthor).text = authorText
    }

    private fun setupDescription() {
        val spannable = SpannableStringBuilder().apply {
            append("\n\nModern çağın getirdiği kısa süreli içerikler\n(reels, shorts, tiktok videoları vb.)\nbeynimizin odaklanma ve derinlemesine\ndüşünme yeteneğini zayıflatıyor.")
            append("\n\nBilimsel araştırmalar, bu tür içeriklerin\nsürekli tüketiminin \"beyin çürümesi\"\n(brain rot) olarak adlandırılan duruma\nyol açtığını gösteriyor.")
            append("\n\nGünde sadece ")
            
            val highlightedText = "\n10 dakika\n"
            val start = length
            append(highlightedText)
            val end = length
            
            setSpan(
                ForegroundColorSpan(Color.parseColor("#FFE500")),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            
            setSpan(
                StyleSpan(Typeface.BOLD),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            
            append("akıl oyunları ve mantık problemleriyle\nzihnini diri tutabilirsin!")
        }

        descriptionText.apply {
            text = spannable
            setTextColor(getColor(R.color.bodyTextColor))
            setShadowLayer(3f, 1f, 1f, Color.parseColor("#40000000"))
        }
    }

    private fun checkLocationPermission() {
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
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
        adManager.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        adManager.onResume()
    }

    override fun onDestroy() {
        adManager.onDestroy()
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setupFullscreen()
        }
    }

    private fun prepareButtonsData(): Any {
        // Add your data preparation logic here
        return Any()
    }

    private fun prepareFeaturesData(): Any {
        // Add your data preparation logic here
        return Any()
    }

    private fun prepareAnimationData(): Any {
        // Add your data preparation logic here
        return Any()
    }

    private fun prepareDescriptionData(): Any {
        // Add your data preparation logic here
        return Any()
    }
}
