package com.brainfocus.numberdetective.di

import com.brainfocus.numberdetective.missions.MissionManager
import com.brainfocus.numberdetective.viewmodel.GameViewModel
import com.brainfocus.numberdetective.viewmodel.LeaderboardViewModel
import com.brainfocus.numberdetective.viewmodel.MissionsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    single { MissionManager.create(get(), get()) }
    
    viewModel { GameViewModel(get()) }
    viewModel { LeaderboardViewModel(get(), get()) }
    viewModel { MissionsViewModel(get(), get()) }
}
