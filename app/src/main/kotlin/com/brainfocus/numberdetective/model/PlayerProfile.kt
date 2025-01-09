package com.brainfocus.numberdetective.model

import com.google.firebase.database.IgnoreExtraProperties

data class Location(
    val district: String? = null,
    val city: String? = null,
    val country: String? = null
)

@IgnoreExtraProperties
data class PlayerProfile(
    val id: String = "",
    val name: String = "",
    val score: Int = 0,
    val location: GameLocation? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to name,
            "score" to score,
            "timestamp" to timestamp,
            "location" to (location?.let {
                mapOf(
                    "district" to (it.district ?: ""),
                    "city" to (it.city ?: ""),
                    "country" to it.country
                )
            } ?: emptyMap<String, String>())
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): PlayerProfile {
            val locationMap = map["location"] as? Map<String, Any>
            return PlayerProfile(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                score = (map["score"] as? Long)?.toInt() ?: 0,
                timestamp = map["timestamp"] as? Long ?: 0,
                location = locationMap?.let {
                    GameLocation(
                        district = it["district"] as? String,
                        city = it["city"] as? String,
                        country = it["country"] as? String ?: "Türkiye"
                    )
                }
            )
        }
    }
}
