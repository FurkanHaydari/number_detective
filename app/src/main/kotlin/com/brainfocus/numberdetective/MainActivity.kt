package com.brainfocus.numberdetective

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.ads.*
import com.google.android.material.button.MaterialButton
import android.view.animation.AnimationUtils
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import android.text.*
import android.text.style.*
import android.graphics.Typeface
import android.graphics.Color
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.os.Build
import kotlinx.coroutines.*
import androidx.appcompat.content.res.AppCompatResources
import com.brainfocus.numberdetective.ads.AdManager

class MainActivity : AppCompatActivity() {
    private lateinit var adView: AdView
    private lateinit var adManager: AdManager
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var descriptionText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupFullscreen()
        
        adView = findViewById(R.id.adView)
        adManager = AdManager.getInstance(this)
        descriptionText = findViewById(R.id.descriptionText)
        
        setupViews()
        loadAds()
        
        mainScope.launch {
            setupButtons()
            setupFeatures()

            coroutineScope {
                delay(100)
                setupAnimation()
                setupQuoteOfDay()
                setupDescription()
            }
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

    private fun loadAds() {
        adManager.initialize {
            adManager.loadBannerAd(adView)
        }
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
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }

        val startButton = findViewById<MaterialButton>(R.id.beyniniKoruButton)
        startButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })
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
                layoutParams = ConstraintLayout.LayoutParams(
                    startButton.width,
                    startButton.height
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
                layoutParams = FrameLayout.LayoutParams(
                    (startButton.width * 0.2).toInt(),
                    startButton.height - 16
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
        // No changes needed here
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

    private fun setupQuoteOfDay() {
        val quotes = resources.getStringArray(R.array.game_quotes)
        val randomQuote = quotes.random()
        val parts = randomQuote.split(" - ")
        
        findViewById<TextView>(R.id.quoteText).text = parts[0]
        findViewById<TextView>(R.id.quoteAuthor).text = "- ${parts[1]}"
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

    override fun onPause() {
        adManager.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        adManager.onResume()
    }

    override fun onDestroy() {
        mainScope.cancel()
        adManager.onDestroy()
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setupFullscreen()
        }
    }
}
