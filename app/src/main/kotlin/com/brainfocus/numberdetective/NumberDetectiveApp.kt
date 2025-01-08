package com.brainfocus.numberdetective

import android.app.Application
import com.brainfocus.numberdetective.di.utilsModule
import com.brainfocus.numberdetective.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import com.google.firebase.FirebaseApp
import com.google.android.gms.ads.MobileAds

class NumberDetectiveApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@NumberDetectiveApp)
            modules(listOf(
                viewModelModule,
                utilsModule
            ))
        }

        // Initialize Firebase and other services once at app startup
        FirebaseApp.initializeApp(this)
        MobileAds.initialize(this)
    }
}
