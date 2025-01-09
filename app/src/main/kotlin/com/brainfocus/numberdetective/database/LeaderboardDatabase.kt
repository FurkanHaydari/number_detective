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
    }

    private val database: DatabaseReference by lazy {
        Firebase.database.reference
    }

    init {
        // Bağlantı durumunu izle
        database.root.child(".info/connected").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    Log.d(TAG, "Connected to Firebase Database")
                } else {
                    Log.w(TAG, "Disconnected from Firebase Database")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase connection listener cancelled: ${error.message}")
            }
        })
    }

    suspend fun updatePlayerScore(userId: String, score: Int, location: GameLocation) {
        try {
            val userData = mapOf(
                "score" to score,
                "timestamp" to System.currentTimeMillis(),
                "location" to mapOf(
                    "district" to location.district,
                    "city" to location.city,
                    "country" to location.country
                )
            )

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

    fun getLeaderboard(location: GameLocation): Flow<List<PlayerProfile>> = callbackFlow {
        val query = database.child(USERS_REF)
            .orderByChild("score")
            .limitToLast(MAX_LEADERBOARD_ENTRIES)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val players = mutableListOf<PlayerProfile>()
                for (child in snapshot.children.reversed()) {
                    val data = child.value as? Map<String, Any> ?: continue
                    val locationData = data["location"] as? Map<String, Any>
                    val playerLocation = locationData?.let {
                        GameLocation(
                            district = it["district"] as? String,
                            city = it["city"] as? String,
                            country = it["country"] as? String ?: "Türkiye"
                        )
                    }

                    if (location.district != null && playerLocation?.district != location.district) continue
                    if (location.city != null && playerLocation?.city != location.city) continue
                    if (playerLocation?.country != location.country) continue

                    players.add(
                        PlayerProfile(
                            id = child.key ?: continue,
                            name = data["name"] as? String ?: "Anonim",
                            score = (data["score"] as? Long)?.toInt() ?: 0,
                            location = playerLocation,
                            timestamp = data["timestamp"] as? Long ?: 0
                        )
                    )
                }
                trySend(players)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error loading leaderboard: ${error.message}")
                close(error.toException())
            }
        }

        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    fun getPlayerStats(userId: String): Flow<PlayerStats> = callbackFlow {
        val statsRef = database.child(STATS_REF).child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val stats = if (snapshot.exists()) {
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
                trySend(stats)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error loading player stats: ${error.message}")
                close(error.toException())
            }
        }

        statsRef.addValueEventListener(listener)
        awaitClose { statsRef.removeEventListener(listener) }
    }
}
