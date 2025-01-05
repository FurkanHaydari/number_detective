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
        val dikkatContainer = findViewById<LinearLayout>(R.id.dikkatContainer)
        val hafizaContainer = findViewById<LinearLayout>(R.id.hafizaContainer) 
        val mantikContainer = findViewById<LinearLayout>(R.id.mantikContainer)
        
        // Animasyonları yükle
        val pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_animation)
        val glowAnimation = AnimationUtils.loadAnimation(this, R.anim.glow_animation)
        
        // Animasyonları başlat
        dikkatContainer.startAnimation(pulseAnimation)
        hafizaContainer.startAnimation(pulseAnimation)
        mantikContainer.startAnimation(pulseAnimation)
        
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
        
        val spannable = SpannableStringBuilder().apply {
            // Başlık
            append("🧠 Beyin çürümesine karşı\n", StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            append("günde 10 dakika!", ForegroundColorSpan(getColor(R.color.neonCyan)), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            
            // Ana metin
            append("\n\nModern teknolojinin getirdiği kısa\nsüreli içerikler, dikkat süremizi ve\nodaklanma yeteneğimizi azaltıyor.")
            append("\n\nBilimsel araştırmalar, günde sadece ")
            
            // Vurgulu metin
            append("10\ndakikalık", ForegroundColorSpan(getColor(R.color.neonCyan)), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            
            // Son kısım
            append(" zihinsel egzersiz bile beyin\nsağlığımızı korumada etkili olduğunu\ngösteriyor.")
        }

        descriptionText.apply {
            text = spannable
            setTextColor(Color.WHITE)
            setShadowLayer(3f, 1f, 1f, Color.parseColor("#40000000"))
        }
    }
}
