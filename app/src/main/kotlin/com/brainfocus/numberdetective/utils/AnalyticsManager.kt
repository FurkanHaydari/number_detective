package com.brainfocus.numberdetective.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsManager private constructor(context: Context) {
    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    companion object {
        @Volatile
        private var instance: AnalyticsManager? = null

        fun getInstance(context: Context): AnalyticsManager {
            return instance ?: synchronized(this) {
                instance ?: AnalyticsManager(context).also { instance = it }
            }
        }
    }

    fun logGameStart() {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, "game_start")
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    fun logGameEnd(score: Int, attempts: Int, timeSeconds: Long, isWin: Boolean) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, "game_end")
            putInt("score", score)
            putInt("attempts", attempts)
            putLong("time_seconds", timeSeconds)
            putBoolean("is_win", isWin)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    fun logAdShown(adType: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, "ad_shown")
            putString("ad_type", adType)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    fun logHighScore(score: Int) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, "new_high_score")
            putInt("score", score)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    fun logError(errorType: String, errorMessage: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, "error")
            putString("error_type", errorType)
            putString("error_message", errorMessage)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }
}
