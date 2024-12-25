package com.brainfocus.numberdetective.utils

import android.content.Context
import android.content.SharedPreferences
import com.brainfocus.numberdetective.model.LeaderboardEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "number_detective_prefs"
        private const val KEY_HIGH_SCORE = "high_score"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_LEADERBOARD = "leaderboard"

        @Volatile
        private var instance: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: PreferencesManager(context).also { instance = it }
            }
        }
    }

    fun saveHighScore(score: Int) {
        if (score > getHighScore()) {
            prefs.edit().putInt(KEY_HIGH_SCORE, score).apply()
        }
    }

    fun getHighScore(): Int {
        return prefs.getInt(KEY_HIGH_SCORE, 0)
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    fun isSoundEnabled(): Boolean {
        return prefs.getBoolean(KEY_SOUND_ENABLED, true)
    }

    fun saveLeaderboardEntry(entry: LeaderboardEntry) {
        val entries = getLeaderboardEntries().toMutableList()
        entries.add(entry)
        entries.sortByDescending { it.score }
        if (entries.size > 100) {
            entries.subList(100, entries.size).clear()
        }
        val json = gson.toJson(entries)
        prefs.edit().putString(KEY_LEADERBOARD, json).apply()
    }

    fun getLeaderboardEntries(): List<LeaderboardEntry> {
        val json = prefs.getString(KEY_LEADERBOARD, null)
        return if (json != null) {
            val type = object : TypeToken<List<LeaderboardEntry>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun clearLeaderboard() {
        prefs.edit().remove(KEY_LEADERBOARD).apply()
    }
}
