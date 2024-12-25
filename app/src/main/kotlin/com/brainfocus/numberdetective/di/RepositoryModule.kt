package com.brainfocus.numberdetective.di

import com.brainfocus.numberdetective.data.repository.GameRepository
import com.brainfocus.numberdetective.data.repository.MissionRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { GameRepository(get(), get()) }
    single { MissionRepository(get(), get()) }
}
