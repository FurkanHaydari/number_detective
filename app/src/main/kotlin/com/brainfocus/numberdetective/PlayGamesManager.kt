package com.brainfocus.numberdetective

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.GamesClient
import com.google.android.gms.games.LeaderboardsClient
import com.google.android.gms.games.Player
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
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
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val TAG = "PlayGamesManager"
        const val RC_LEADERBOARD_UI = 9004
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    suspend fun initialize(activity: Activity) {
        try {
            leaderboardsClient = PlayGames.getLeaderboardsClient(activity)
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Play Games", e)
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    suspend fun submitScore(score: Long) {
        try {
            leaderboardsClient?.let { client ->
                client.submitScoreImmediate(context.getString(R.string.leaderboard_global_all_time), score).await()
                submitLocalScore(score)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting score", e)
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private suspend fun submitLocalScore(score: Long) {
        try {
            leaderboardsClient?.let { client ->
                client.submitScoreImmediate(context.getString(R.string.leaderboard_location_based), score).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting local score", e)
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

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private suspend fun getCurrentLocation(activity: Activity): Location? {
        return try {
            if (hasLocationPermission(activity)) {
                fusedLocationClient.lastLocation.await()
            } else {
                requestLocationPermission(activity)
                null
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            null
        }
    }

    private fun hasLocationPermission(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }


}
