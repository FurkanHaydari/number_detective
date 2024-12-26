package com.brainfocus.numberdetective.di

import com.brainfocus.numberdetective.viewmodel.GameViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { GameViewModel() }
}
