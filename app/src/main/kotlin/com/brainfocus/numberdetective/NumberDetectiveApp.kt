package com.brainfocus.numberdetective

import android.app.Application
import android.content.Context
import com.brainfocus.numberdetective.core.utils.LocaleHelper
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NumberDetectiveApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize AdMob
        MobileAds.initialize(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }
}