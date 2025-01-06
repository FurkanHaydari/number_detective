package com.brainfocus.numberdetective

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.LoadAdError
import com.google.android.material.button.MaterialButton
import com.airbnb.lottie.LottieAnimationView
import java.util.Calendar
import android.widget.TextView
import android.view.animation.AnimationUtils
import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.text.style.ForegroundColorSpan
import android.graphics.Typeface
import android.text.Spanned
import android.graphics.Color
import android.widget.ImageView

class MainActivity : AppCompatActivity() {
    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupAds()
        setupButtons()
        setupAnimation()
        setupQuoteOfDay()
        setupDescription()
        setupFeatures()
    }

    private fun setupAds() {
        MobileAds.initialize(this)
        
        adView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        
        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d("Ads", "Main activity ad loaded successfully")
            }
            
            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.d("Ads", "Main activity ad failed to load: ${error.message}")
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

    private fun setupButtons() {
        findViewById<MaterialButton>(R.id.beyniniKoruButton).setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
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
                background = getDrawable(R.drawable.button_glow_overlay)
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
            setTextColor(getColor(R.color.quoteTextColor))
            setShadowLayer(2f, 0f, 1f, Color.parseColor("#40000000"))
        }
        
        val spannable = SpannableStringBuilder().apply {
            // Ana metin
            append("\n\nModern teknolojinin getirdiği kısa\nsüreli içerikler, dikkat süremizi ve\nodaklanma yeteneğimizi azaltıyor.")
            append("\n\nBilimsel araştırmalar, günde sadece ")
            
            // Vurgulu metin (daha parlak)
            append("10\ndakikalık", ForegroundColorSpan(getColor(R.color.neonCyan)), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            
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
        val features = listOf(
            Triple("dikkatFeature", R.drawable.dikkat, "Dikkat"),
            Triple("hafizaFeature", R.drawable.hafiza, "Hafıza"),
            Triple("mantikFeature", R.drawable.mantik, "Mantık")
        )
        
        features.forEach { (id, iconRes, title) ->
            val featureView = findViewById<ViewGroup>(resources.getIdentifier(id, "id", packageName))
            featureView.findViewById<ImageView>(R.id.featureIcon).setImageResource(iconRes)
            featureView.findViewById<TextView>(R.id.featureTitle).text = title
        }
    }
}
