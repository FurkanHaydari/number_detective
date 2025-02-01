package com.brainfocus.numberdetective.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {
    // ViewModels with @HiltViewModel and constructor injection don't need providers
}