package com.brainfocus.numberdetective

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.material.button.MaterialButton
import java.util.Calendar
import android.widget.TextView
import android.view.animation.AnimationUtils
import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.text.style.ForegroundColorSpan
import android.graphics.Typeface
import android.text.style.RelativeSizeSpan
import android.graphics.Color
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.os.Build
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.*
import androidx.appcompat.content.res.AppCompatResources
import android.text.Spanned

class MainActivity : AppCompatActivity() {
    private var _adView: AdView? = null
    private lateinit var adView: AdView
    
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupFullscreen()
        
        // Handle back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })
        
        // Configure AdMob test device
        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(listOf("2AB9450CDEBBD309C545CF4327C4FB6C"))
            .build()
        MobileAds.setRequestConfiguration(configuration)
        
        // Initialize AdMob
        MobileAds.initialize(this) {
            // Then setup ads after initialization
            setupAds()
        }
        
        // UI işlemlerini sırayla yapalım
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
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    private fun setupAds() {
        val adContainer = findViewById<FrameLayout>(R.id.adContainer)
        adView = findViewById(R.id.adView)
        _adView = adView
        
        // Container ve AdView'ı başlangıçta görünmez yap
        adContainer.alpha = 0f
        adView.alpha = 0f
        
        // Reklam yüklemesini main thread'de yapıyoruz
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d("Ads", "Main activity ad loaded successfully")
                
                // Önce container'ı fade in yap
                adContainer.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .withEndAction {
                        // Sonra AdView'ı fade in yap
                        adView.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .start()
                    }
                    .start()
            }
            
            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e("Ads", "Ad failed to load: ${error.message}")
            }
        }
    }

    override fun onPause() {
        _adView?.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        _adView?.resume()
    }

    override fun onDestroy() {
        mainScope.cancel() // Coroutine'leri temizle
        _adView?.destroy()
        _adView = null
        super.onDestroy()
    }

    private fun setupButtons() {
        findViewById<MaterialButton>(R.id.beyniniKoruButton).setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

        findViewById<MaterialButton>(R.id.leaderboardButton).setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }

        // Handle back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })
    }

    private fun setupAnimation() {
        // Feature container'ları bul
        val dikkatFeature = findViewById<ViewGroup>(R.id.dikkatFeature)
        val hafizaFeature = findViewById<ViewGroup>(R.id.hafizaFeature)
        val mantikFeature = findViewById<ViewGroup>(R.id.mantikFeature)
        
        // Animasyonları yükle
        val pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_animation)
        val glowAnimation = AnimationUtils.loadAnimation(this, R.anim.glow_animation)
        
        // Animasyonları başlat
        dikkatFeature.startAnimation(pulseAnimation)
        hafizaFeature.startAnimation(pulseAnimation)
        mantikFeature.startAnimation(pulseAnimation)
        
        // Buton animasyonu
        val startButton = findViewById<MaterialButton>(R.id.beyniniKoruButton)
        
        startButton.post {
            // Butonun parent'ını bul
            val parent = startButton.parent as ViewGroup
            val buttonIndex = parent.indexOfChild(startButton)
            
            // FrameLayout oluştur ve constraint parametrelerini kopyala
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
                clipChildren = true  // Çocuk view'ların dışarı taşmasını engelle
                clipToOutline = true // Outline'a göre kırp
            }
            
            // Butonu parent'ından kaldır ve frame'e ekle
            parent.removeView(startButton)
            frameLayout.addView(startButton, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ))
            
            // Overlay view oluştur
            val overlayView = View(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    (startButton.width * 0.2).toInt(), // Buton genişliğinin %20'si
                    startButton.height - 16 // Kenarlardan biraz içeride
                ).apply {
                    gravity = android.view.Gravity.CENTER
                    setMargins(8, 8, 8, 8) // Kenarlardan margin
                }
                background = AppCompatResources.getDrawable(context, R.drawable.button_glow_overlay)
                elevation = startButton.elevation - 1f
            }
            
            // Overlay'i frame'e ekle
            frameLayout.addView(overlayView)
            
            // Frame'i orijinal pozisyona ekle
            parent.addView(frameLayout, buttonIndex)
            
            // Animasyonu başlat
            overlayView.startAnimation(glowAnimation)
        }
        
        startButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
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
        val descriptionText = findViewById<TextView>(R.id.descriptionText)
        val titleText = findViewById<TextView>(R.id.titleText)
        val quoteText = findViewById<TextView>(R.id.quoteText)
        
        // Başlık stili
        titleText.apply {
            setTextColor(getColor(R.color.titleTextColor))
            setShadowLayer(4f, 0f, 2f, Color.parseColor("#40000000"))
        }
        
        // Alıntı stili
        quoteText.apply {
            val quoteSpannable = SpannableStringBuilder().apply {
                // Tırnak işareti
                append("❝   ", StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(
                    RelativeSizeSpan(1.4f), // Tırnak işaretini %40 daha büyük yap
                    0,
                    1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpan(
                    ForegroundColorSpan(getColor(R.color.quoteTextColor)),
                    0,
                    1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                
                // Alıntı metni
                append(text)
            }
            
            text = quoteSpannable
            setTextColor(getColor(R.color.quoteTextColor))
            setShadowLayer(2f, 0f, 1f, Color.parseColor("#40000000"))
        }
        
        val spannable = SpannableStringBuilder().apply {
            // Ana metin
            append("\n\nModern teknolojinin getirdiği kısa\nsüreli içerikler, dikkat süremizi ve\nodaklanma yeteneğimizi azaltıyor.")
            append("\n\nBilimsel araştırmalar, günde sadece ")
            
            // Vurgulu metin (daha parlak ve farklı renk)
            val highlightedText = "10 dakikalık"
            val start = length
            append(highlightedText)
            val end = length
            
            // Renk ve stil efektleri
            setSpan(
                ForegroundColorSpan(Color.parseColor("#FFE500")), // Parlak sarı
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
            
            // Son kısım
            append(" zihinsel egzersiz bile beyin\nsağlığımızı korumada etkili olduğunu\ngösteriyor.")
        }

        descriptionText.apply {
            text = spannable
            setTextColor(getColor(R.color.bodyTextColor))
            setShadowLayer(3f, 1f, 1f, Color.parseColor("#40000000"))
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
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setupFullscreen()
        }
    }
}
