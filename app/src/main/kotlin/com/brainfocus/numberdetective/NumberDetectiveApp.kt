package com.brainfocus.numberdetective

import android.app.Application
import com.brainfocus.numberdetective.di.utilsModule
import com.brainfocus.numberdetective.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

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
    }
}
