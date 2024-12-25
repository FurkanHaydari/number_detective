package com.brainfocus.numberdetective.di

import com.brainfocus.numberdetective.utils.ErrorHandler
import com.brainfocus.numberdetective.utils.PreferencesManager
import com.brainfocus.numberdetective.utils.SoundManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val utilModule = module {
    single { ErrorHandler() }
    single { PreferencesManager.getInstance(androidContext()) }
    single { SoundManager.getInstance(androidContext()) }
}
