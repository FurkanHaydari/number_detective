package com.brainfocus.numberdetective

import android.app.Application
import com.brainfocus.numberdetective.di.databaseModule
import com.brainfocus.numberdetective.di.repositoryModule
import com.brainfocus.numberdetective.di.utilModule
import com.brainfocus.numberdetective.di.viewModelModule
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class NumberDetectiveApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Firebase başlatma
        FirebaseApp.initializeApp(this)

        // Koin başlatma
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@NumberDetectiveApplication)
            modules(
                listOf(
                    databaseModule,
                    repositoryModule,
                    viewModelModule,
                    utilModule
                )
            )
        }
    }
}
