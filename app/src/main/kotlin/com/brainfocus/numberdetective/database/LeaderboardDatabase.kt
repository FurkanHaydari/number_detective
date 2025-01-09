package com.brainfocus.numberdetective.database

import android.util.Log
import com.brainfocus.numberdetective.model.GameLocation
import com.brainfocus.numberdetective.model.PlayerProfile
import com.brainfocus.numberdetective.model.PlayerStats
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class LeaderboardDatabase {
    companion object {
        private const val TAG = "LeaderboardDatabase"
        private const val USERS_REF = "users"
        private const val STATS_REF = "stats"
        private const val MAX_LEADERBOARD_ENTRIES = 100
        private const val DATABASE_URL = "https://number-detective-686e2-default-rtdb.europe-west1.firebasedatabase.app"
    }

    private val database: DatabaseReference = Firebase.database(DATABASE_URL).reference

    init {
        database.root.child(".info/connected").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    Log.d(TAG, "Connected to Firebase Database")
                } else {
                    Log.w(TAG, "Disconnected from Firebase Database")
                    reconnectToDatabase()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase connection listener cancelled: ${error.message}")
                reconnectToDatabase()
            }
        })
    }

    private fun reconnectToDatabase() {
        try {
            Firebase.database(DATABASE_URL).setPersistenceEnabled(true)
            Log.d(TAG, "Attempting to reconnect to Firebase Database")
        } catch (e: Exception) {
            Log.e(TAG, "Error reconnecting to database: ${e.message}")
        }
    }

    suspend fun updatePlayerScore(userId: String, score: Int, location: GameLocation) {
        try {
            val userData = PlayerProfile(
                userId = userId,
                displayName = "Anonim Oyuncu",  // TODO: Gerçek kullanıcı adını al
                score = score,
                location = location
            ).toMap()

            database.child(USERS_REF)
                .child(userId)
                .updateChildren(userData)
                .await()
            
            updatePlayerStats(userId, score)
            Log.d(TAG, "Player score updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating player score: ${e.message}")
            throw e
        }
    }

    private suspend fun updatePlayerStats(userId: String, score: Int) {
        try {
            val statsRef = database.child(STATS_REF).child(userId)
            val snapshot = statsRef.get().await()
            
            val currentStats = if (snapshot.exists()) {
                val data = snapshot.value as Map<String, Any>
                PlayerStats(
                    totalGames = (data["totalGames"] as? Long)?.toInt() ?: 0,
                    wins = (data["wins"] as? Long)?.toInt() ?: 0,
                    losses = (data["losses"] as? Long)?.toInt() ?: 0,
                    highScore = (data["highScore"] as? Long)?.toInt() ?: 0,
                    averageScore = (data["averageScore"] as? Double) ?: 0.0,
                    totalScore = (data["totalScore"] as? Long)?.toInt() ?: 0,
                    lastPlayed = data["lastPlayed"] as? Long ?: 0
                )
            } else {
                PlayerStats()
            }

            val newStats = currentStats.copy(
                totalGames = currentStats.totalGames + 1,
                wins = if (score > 0) currentStats.wins + 1 else currentStats.wins,
                losses = if (score == 0) currentStats.losses + 1 else currentStats.losses,
                highScore = maxOf(currentStats.highScore, score),
                totalScore = currentStats.totalScore + score,
                averageScore = (currentStats.totalScore + score).toDouble() / (currentStats.totalGames + 1),
                lastPlayed = System.currentTimeMillis()
            )

            statsRef.setValue(newStats).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating player stats: ${e.message}")
            throw e
        }
    }

    suspend fun getDistrictLeaderboard(district: String): List<PlayerProfile> {
        return try {
            val snapshot = database.child(USERS_REF)
                .orderByChild("location/district")
                .equalTo(district)
                .limitToLast(MAX_LEADERBOARD_ENTRIES)
                .get()
                .await()

            val profiles = mutableListOf<PlayerProfile>()
            snapshot.children.forEach { child ->
                val data = child.value as? Map<String, Any>
                if (data != null) {
                    profiles.add(PlayerProfile.fromMap(data))
                }
            }
            
            profiles.sortedByDescending { it.score }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting district leaderboard: ${e.message}")
            emptyList()
        }
    }

    fun getLeaderboard(location: GameLocation): Flow<List<PlayerProfile>> = callbackFlow {
        val query = database.child(USERS_REF)
            .orderByChild("score")
            .limitToLast(MAX_LEADERBOARD_ENTRIES)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profiles = mutableListOf<PlayerProfile>()
                snapshot.children.forEach { child ->
                    val data = child.value as? Map<String, Any>
                    if (data != null) {
                        profiles.add(PlayerProfile.fromMap(data))
                    }
                }
                trySend(profiles.sortedByDescending { it.score })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error loading leaderboard: ${error.message}")
                trySend(emptyList())
            }
        }

        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    suspend fun getPlayer(userId: String): PlayerProfile? {
        return try {
            val snapshot = database.child(USERS_REF).child(userId).get().await()
            if (snapshot.exists()) {
                PlayerProfile.fromMap(snapshot.value as Map<String, Any>)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting player: ${e.message}")
            null
        }
    }
}
