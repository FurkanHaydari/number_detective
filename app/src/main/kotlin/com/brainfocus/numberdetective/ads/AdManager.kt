package com.brainfocus.numberdetective.ads

import android.content.Context
import android.util.Log
import com.brainfocus.numberdetective.BuildConfig
import com.brainfocus.numberdetective.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.*
import android.app.Activity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AdManagerModule {
    @Provides
    @Singleton
    fun provideAdManager(@ApplicationContext context: Context): AdManager {
        return AdManager(context)
    }
}

@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var interstitialAd: InterstitialAd? = null
    private var isInitialized = false
    private var isLoading = false
    private var retryAttempt = 0
    private val maxRetryAttempts = 3
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    
    companion object {
        private const val TAG = "AdManager"
        private const val RETRY_DELAY = 5000L // 5 seconds
    }
    
    fun initialize() {
        if (isInitialized) return
        
        MobileAds.initialize(context) {
            // Log.d(TAG, "Mobile Ads initialized successfully")
            loadInterstitialAd()
        }
        
        isInitialized = true
    }
    
    private fun loadInterstitialAd() {
        if (isLoading) return
        
        isLoading = true
        val adRequest = AdRequest.Builder().build()
        val adUnitId = context.getString(R.string.interstitial_ad_unit_id)
        
        // Log.d(TAG, "Loading interstitial ad, attempt: ${retryAttempt + 1}")
        
        InterstitialAd.load(context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "Failed to load interstitial ad: ${adError.message}")
                isLoading = false
                interstitialAd = null
                
                if (retryAttempt < maxRetryAttempts) {
                    retryAttempt++
                    retryLoadingAd()
                } else {
                    Log.e(TAG, "Max retry attempts reached for loading interstitial ad")
                    retryAttempt = 0
                }
            }
            
            override fun onAdLoaded(ad: InterstitialAd) {
                // Log.d(TAG, "Interstitial ad loaded successfully")
                isLoading = false
                interstitialAd = ad
                retryAttempt = 0
                setupAdCallbacks(ad)
            }
        })
    }
    
    private fun retryLoadingAd() {
        coroutineScope.launch {
            delay(RETRY_DELAY)
            loadInterstitialAd()
        }
    }
    
    fun showInterstitialAd() {
        val activity = context as? Activity
        if (activity == null) {
            Log.e(TAG, "Context is not an Activity")
            return
        }
        
        val ad = interstitialAd
        if (ad != null) {
            ad.show(activity)
        } else {
            // Log.d(TAG, "Interstitial ad was not ready")
            loadInterstitialAd()
        }
    }
    
    private fun setupAdCallbacks(ad: InterstitialAd) {
        ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                // Log.d(TAG, "Ad dismissed fullscreen content")
                interstitialAd = null
                loadInterstitialAd() // Load the next ad
            }
            
            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                Log.e(TAG, "Ad failed to show fullscreen content: ${error.message}")
                interstitialAd = null
                loadInterstitialAd() // Try loading another ad
            }
            
            override fun onAdShowedFullScreenContent() {
                // Log.d(TAG, "Ad showed fullscreen content")
            }
        }
    }
    
    fun onPause() {
        // No specific action needed for interstitial ads on pause
    }
    
    fun onResume() {
        // Load a new ad if none is available
        if (interstitialAd == null && !isLoading) {
            loadInterstitialAd()
        }
    }
    
    fun release() {
        coroutineScope.cancel()
        interstitialAd = null
        isInitialized = false
        isLoading = false
        retryAttempt = 0
    }
}
