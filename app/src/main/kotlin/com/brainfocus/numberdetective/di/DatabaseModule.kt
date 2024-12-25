package com.brainfocus.numberdetective.di

import com.brainfocus.numberdetective.data.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { AppDatabase.getInstance(androidContext()) }
    single { get<AppDatabase>().gameResultDao() }
    single { get<AppDatabase>().missionDao() }
}
