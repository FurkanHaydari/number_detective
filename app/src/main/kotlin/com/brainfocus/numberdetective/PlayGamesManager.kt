package com.brainfocus.numberdetective

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.LeaderboardsClient
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.tasks.await
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayGamesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var leaderboardsClient: LeaderboardsClient? = null

    companion object {
        private const val TAG = "PlayGamesManager"
        const val RC_LEADERBOARD_UI = 9004
    }

    suspend fun initialize(activity: Activity) {
        try {
            leaderboardsClient = PlayGames.getLeaderboardsClient(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Play Games", e)
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    suspend fun submitScore(score: Long) {
        try {
            if (score <= 300) {
                Log.d(TAG, "Score is too low, skipping submission")
                return
            }
            leaderboardsClient?.let { client ->
                client.submitScoreImmediate(context.getString(R.string.leaderboard_global_all_time), score).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting score", e)
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }
    fun showLeaderboard(activity: Activity, leaderboardId: String) {
        try {
            leaderboardsClient?.let { client ->
                val task = client.getLeaderboardIntent(leaderboardId)
                task.addOnSuccessListener { intent ->
                    activity.startActivityForResult(intent, RC_LEADERBOARD_UI)
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error showing leaderboard", e)
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing leaderboard", e)
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    fun showAllLeaderboards(activity: Activity) {
        try {
            leaderboardsClient?.let { client ->
                val task = client.allLeaderboardsIntent
                task.addOnSuccessListener { intent ->
                    activity.startActivityForResult(intent, RC_LEADERBOARD_UI)
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error showing all leaderboards", e)
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing all leaderboards", e)
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

}
