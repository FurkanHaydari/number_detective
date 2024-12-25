package com.brainfocus.numberdetective.di

import com.brainfocus.numberdetective.utils.ErrorHandler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val utilsModule = module {
    single { ErrorHandler(androidContext()) }
}
