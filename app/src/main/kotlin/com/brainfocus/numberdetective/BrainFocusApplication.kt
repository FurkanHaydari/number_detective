package com.brainfocus.numberdetective

import android.app.Application
import com.brainfocus.numberdetective.di.viewModelModule
import com.brainfocus.numberdetective.utils.ImageUtils
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BrainFocusApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize Koin
        startKoin {
            androidContext(this@BrainFocusApplication)
            modules(viewModelModule)
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_MODERATE) {
            ImageUtils.clearCache()
        }
    }
}
