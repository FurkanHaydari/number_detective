package com.brainfocus.numberdetective.database

import android.util.Log
import com.brainfocus.numberdetective.model.PlayerProfile
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class LeaderboardDatabase {
    companion object {
        private const val TAG = "LeaderboardDatabase"
        private const val LEADERBOARDS_REF = "leaderboards"
        private const val MAX_LEADERBOARD_ENTRIES = 100
    }

    private val database: DatabaseReference by lazy {
        Firebase.database.apply {
            setPersistenceEnabled(true)
        }.reference
    }

    suspend fun updatePlayerScore(city: String, player: PlayerProfile) {
        try {
            val playerRef = database.child(LEADERBOARDS_REF)
                .child(city)
                .child(player.playerId)

            playerRef.setValue(player.toMap()).await()
            Log.d(TAG, "Player score updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating player score: ${e.message}")
            throw e
        }
    }

    fun getTopPlayers(city: String): Flow<List<PlayerProfile>> = callbackFlow {
        val leaderboardRef = database.child(LEADERBOARDS_REF)
            .child(city)
            .orderByChild("highScore")
            .limitToLast(MAX_LEADERBOARD_ENTRIES)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val players = mutableListOf<PlayerProfile>()
                    for (childSnapshot in snapshot.children.reversed()) {
                        childSnapshot.getValue(PlayerProfile::class.java)?.let { player ->
                            players.add(player)
                        }
                    }
                    trySend(players.sortedByDescending { it.highScore })
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing players: ${e.message}")
                    trySend(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database error: ${error.message}")
                trySend(emptyList())
            }
        }

        leaderboardRef.addValueEventListener(listener)
        
        awaitClose {
            leaderboardRef.removeEventListener(listener)
        }
    }

    suspend fun getPlayerProfile(city: String, playerId: String): PlayerProfile? {
        return try {
            val snapshot = database.child(LEADERBOARDS_REF)
                .child(city)
                .child(playerId)
                .get()
                .await()

            val playerMap = snapshot.value as? Map<String, Any>
            playerMap?.let { PlayerProfile.fromMap(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting player profile: ${e.message}")
            null
        }
    }

    suspend fun updatePlayerStats(city: String, playerId: String, gamesPlayed: Int, lastPlayed: Long) {
        try {
            val updates = hashMapOf<String, Any>(
                "totalGames" to gamesPlayed,
                "lastPlayed" to lastPlayed
            )

            database.child(LEADERBOARDS_REF)
                .child(city)
                .child(playerId)
                .updateChildren(updates)
                .await()

            Log.d(TAG, "Player stats updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating player stats: ${e.message}")
            throw e
        }
    }
}
