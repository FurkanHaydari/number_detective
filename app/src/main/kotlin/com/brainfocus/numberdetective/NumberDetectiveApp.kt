package com.brainfocus.numberdetective

import android.app.Application
import com.brainfocus.numberdetective.di.databaseModule
import com.brainfocus.numberdetective.di.repositoryModule
import com.brainfocus.numberdetective.di.utilsModule
import com.brainfocus.numberdetective.di.viewModelModule
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class NumberDetectiveApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize Koin
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@NumberDetectiveApp)
            modules(
                listOf(
                    databaseModule,
                    repositoryModule,
                    utilsModule,
                    viewModelModule
                )
            )
        }
    }
}
