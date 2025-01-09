package com.brainfocus.numberdetective.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class PlayerProfile(
    val playerId: String = "",
    val displayName: String = "",
    val location: String = "",
    val highScore: Int = 0,
    val totalGames: Int = 0,
    val lastPlayed: Long = 0
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "playerId" to playerId,
            "displayName" to displayName,
            "location" to location,
            "highScore" to highScore,
            "totalGames" to totalGames,
            "lastPlayed" to lastPlayed
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): PlayerProfile {
            return PlayerProfile(
                playerId = map["playerId"] as? String ?: "",
                displayName = map["displayName"] as? String ?: "",
                location = map["location"] as? String ?: "",
                highScore = (map["highScore"] as? Long)?.toInt() ?: 0,
                totalGames = (map["totalGames"] as? Long)?.toInt() ?: 0,
                lastPlayed = map["lastPlayed"] as? Long ?: 0
            )
        }
    }
}
