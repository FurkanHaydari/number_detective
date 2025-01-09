package com.brainfocus.numberdetective.repository

import android.content.Context
import android.util.Log
import com.brainfocus.numberdetective.database.LeaderboardDatabase
import com.brainfocus.numberdetective.location.GameLocationManager
import com.brainfocus.numberdetective.model.PlayerProfile
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit

class LeaderboardRepository(
    private val context: Context,
    private val database: LeaderboardDatabase = LeaderboardDatabase(),
    private val locationManager: GameLocationManager = GameLocationManager(context)
) {
    companion object {
        private const val TAG = "LeaderboardRepository"
        private const val DEFAULT_CITY = "unknown"
    }

    suspend fun updatePlayerScore(account: GoogleSignInAccount, score: Int) {
        try {
            val city = locationManager.getCurrentCity() ?: DEFAULT_CITY
            
            // Mevcut profili al veya yeni oluştur
            val existingProfile = database.getPlayerProfile(city, account.id!!)
            val updatedProfile = existingProfile?.copy(
                highScore = maxOf(existingProfile.highScore, score),
                totalGames = existingProfile.totalGames + 1,
                lastPlayed = System.currentTimeMillis()
            ) ?: PlayerProfile(
                playerId = account.id!!,
                displayName = account.displayName ?: "Anonymous",
                location = city,
                highScore = score,
                totalGames = 1,
                lastPlayed = System.currentTimeMillis()
            )

            database.updatePlayerScore(city, updatedProfile)
            Log.d(TAG, "Player score updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating player score: ${e.message}")
            throw e
        }
    }

    fun getTopPlayers(): Flow<List<PlayerProfile>> = flow {
        try {
            val city = locationManager.getCurrentCity() ?: DEFAULT_CITY
            database.getTopPlayers(city).collect { players ->
                emit(players)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting top players: ${e.message}")
            emit(emptyList())
        }
    }

    fun hasLocationPermission(): Boolean {
        return locationManager.hasLocationPermission()
    }

    suspend fun getPlayerStats(account: GoogleSignInAccount): PlayerProfile? {
        return try {
            val city = locationManager.getCurrentCity() ?: DEFAULT_CITY
            database.getPlayerProfile(city, account.id!!)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting player stats: ${e.message}")
            null
        }
    }

    private fun formatTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "az önce"
            diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} dakika önce"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)} saat önce"
            else -> "${TimeUnit.MILLISECONDS.toDays(diff)} gün önce"
        }
    }
}
