package com.brainfocus.numberdetective.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "number_detective_prefs")

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKeys {
        val HIGH_SCORE = intPreferencesKey("high_score")
        val CURRENT_LEVEL = intPreferencesKey("current_level")
        val REMAINING_ATTEMPTS = intPreferencesKey("remaining_attempts")
        val REMAINING_TIME = intPreferencesKey("remaining_time")
        val CURRENT_SCORE = intPreferencesKey("current_score")
        val CORRECT_ANSWER = stringPreferencesKey("correct_answer")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val IS_SOUND_ENABLED = booleanPreferencesKey("is_sound_enabled")
        val IS_HELPER_MODE_ENABLED = booleanPreferencesKey("is_helper_mode_enabled")
    }

    // High Score Flow
    val highScoreFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HIGH_SCORE] ?: 0
    }

    suspend fun saveHighScore(score: Int) {
        context.dataStore.edit { preferences ->
            val currentHighScore = preferences[PreferencesKeys.HIGH_SCORE] ?: 0
            if (score > currentHighScore) {
                preferences[PreferencesKeys.HIGH_SCORE] = score
            }
        }
    }

    // Game State Flow (Allows resuming an incomplete game)
    val currentLevelFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CURRENT_LEVEL] ?: 1
    }

    val remainingAttemptsFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMAINING_ATTEMPTS] ?: -1
    }

    val remainingTimeFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMAINING_TIME] ?: -1
    }
    
    val currentScoreFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CURRENT_SCORE] ?: 0
    }

    val correctAnswerFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CORRECT_ANSWER]
    }

    val isFirstLaunchFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_FIRST_LAUNCH] ?: true
    }

    val isSoundEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_SOUND_ENABLED] ?: true
    }

    val isHelperModeEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_HELPER_MODE_ENABLED] ?: false
    }

    suspend fun toggleSound(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_SOUND_ENABLED] = enabled
        }
    }

    suspend fun toggleHelperMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_HELPER_MODE_ENABLED] = enabled
        }
    }

    suspend fun completeOnboarding() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_LAUNCH] = false
        }
    }

    suspend fun saveGameState(
        level: Int,
        attempts: Int,
        time: Int,
        score: Int,
        answer: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_LEVEL] = level
            preferences[PreferencesKeys.REMAINING_ATTEMPTS] = attempts
            preferences[PreferencesKeys.REMAINING_TIME] = time
            preferences[PreferencesKeys.CURRENT_SCORE] = score
            preferences[PreferencesKeys.CORRECT_ANSWER] = answer
        }
    }

    suspend fun clearGameState() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.CURRENT_LEVEL)
            preferences.remove(PreferencesKeys.REMAINING_ATTEMPTS)
            preferences.remove(PreferencesKeys.REMAINING_TIME)
            preferences.remove(PreferencesKeys.CURRENT_SCORE)
            preferences.remove(PreferencesKeys.CORRECT_ANSWER)
        }
    }
}
