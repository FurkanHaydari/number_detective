package com.brainfocus.numberdetective.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdManager private constructor(private val context: Context) {
    private var mInterstitialAd: InterstitialAd? = null
    private var isAdsInitialized = false

    companion object {
        private const val TAG = "AdManager"
        @Volatile private var instance: AdManager? = null

        fun getInstance(context: Context): AdManager {
            return instance ?: synchronized(this) {
                instance ?: AdManager(context).also { instance = it }
            }
        }

        fun buildAdRequest(): AdRequest {
            return AdRequest.Builder().build()
        }
    }

    fun initialize(onInitialized: () -> Unit = {}) {
        if (!isAdsInitialized) {
            MobileAds.initialize(context) { _ ->
                isAdsInitialized = true
                onInitialized()
            }
        } else {
            onInitialized()
        }
    }

    fun loadBannerAd(adView: AdView) {
        try {
            val adRequest = buildAdRequest()
            adView.loadAd(adRequest)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading banner ad: ${e.message}")
        }
    }

    fun loadInterstitialAd(
        activity: Activity,
        adUnitId: String,
        onAdLoaded: () -> Unit = {},
        onAdFailedToLoad: () -> Unit = {}
    ) {
        val adRequest = buildAdRequest()
        InterstitialAd.load(activity, adUnitId, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    onAdLoaded()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    mInterstitialAd = null
                    onAdFailedToLoad()
                }
            })
    }

    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit) {
        val ad = mInterstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mInterstitialAd = null
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    mInterstitialAd = null
                    onAdDismissed()
                }
            }
            ad.show(activity)
        } else {
            onAdDismissed()
        }
    }

    fun hasLoadedInterstitialAd(): Boolean {
        return mInterstitialAd != null
    }

    fun onPause() {
        // İleride gerekirse buraya kod eklenebilir
    }

    fun onResume() {
        // İleride gerekirse buraya kod eklenebilir
    }

    fun onDestroy() {
        mInterstitialAd = null
    }
}
