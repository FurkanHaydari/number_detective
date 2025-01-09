package com.brainfocus.numberdetective

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.android.gms.ads.MobileAds
import com.brainfocus.numberdetective.di.viewModelModule

class NumberDetectiveApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@NumberDetectiveApp)
            modules(listOf(
                viewModelModule
            ))
        }

        // Firebase persistence'ı etkinleştir
        Firebase.database.setPersistenceEnabled(true)
        
        // AdMob başlat
        MobileAds.initialize(this)
    }
} 