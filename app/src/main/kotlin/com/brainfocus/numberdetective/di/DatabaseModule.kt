package com.brainfocus.numberdetective.di

import android.content.Context
import com.brainfocus.numberdetective.data.AppDatabase
import com.brainfocus.numberdetective.data.dao.GameResultDao
import com.brainfocus.numberdetective.data.dao.MissionDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { provideAppDatabase(androidContext()) }
    single { provideGameResultDao(get()) }
    single { provideMissionDao(get()) }
}

private fun provideAppDatabase(context: Context): AppDatabase {
    return AppDatabase.getInstance(context)
}

private fun provideGameResultDao(database: AppDatabase): GameResultDao {
    return database.gameResultDao()
}

private fun provideMissionDao(database: AppDatabase): MissionDao {
    return database.missionDao()
}
