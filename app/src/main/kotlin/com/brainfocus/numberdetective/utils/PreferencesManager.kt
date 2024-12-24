package com.brainfocus.numberdetective.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "BrainFocusPrefs"
        private const val KEY_HIGH_SCORE = "high_score"
        private const val KEY_TOTAL_GAMES = "total_games"
        private const val KEY_TOTAL_WINS = "total_wins"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_PLAYER_NAME = "player_name"

        @Volatile
        private var instance: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: PreferencesManager(context).also { instance = it }
            }
        }
    }

    fun updateHighScore(score: Int) {
        val currentHighScore = getHighScore()
        if (score > currentHighScore) {
            prefs.edit().putInt(KEY_HIGH_SCORE, score).apply()
        }
    }

    fun getHighScore(): Int = prefs.getInt(KEY_HIGH_SCORE, 0)

    fun incrementTotalGames() {
        val currentTotal = getTotalGames()
        prefs.edit().putInt(KEY_TOTAL_GAMES, currentTotal + 1).apply()
    }

    fun getTotalGames(): Int = prefs.getInt(KEY_TOTAL_GAMES, 0)

    fun incrementTotalWins() {
        val currentWins = getTotalWins()
        prefs.edit().putInt(KEY_TOTAL_WINS, currentWins + 1).apply()
    }

    fun getTotalWins(): Int = prefs.getInt(KEY_TOTAL_WINS, 0)

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    fun isSoundEnabled(): Boolean = prefs.getBoolean(KEY_SOUND_ENABLED, true)

    fun setPlayerName(name: String) {
        prefs.edit().putString(KEY_PLAYER_NAME, name).apply()
    }

    fun getPlayerName(): String = prefs.getString(KEY_PLAYER_NAME, "") ?: ""

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
