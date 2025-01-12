package com.brainfocus.numberdetective.model

import com.google.firebase.database.IgnoreExtraProperties

data class Location(
    val district: String? = null,
    val city: String? = null,
    val country: String? = null
)

@IgnoreExtraProperties
data class PlayerProfile(
    val userId: String = "",
    val displayName: String = "",
    val score: Int = 0,
    val location: GameLocation? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "displayName" to displayName,
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
            return try {
                val locationData = map["location"] as? Map<*, *>
                val locationMap = locationData?.mapNotNull { (key, value) ->
                    key?.toString()?.let { k -> value?.let { v -> k to v } }
                }?.toMap()

                PlayerProfile(
                    userId = map["userId"]?.toString() ?: "",
                    displayName = map["displayName"]?.toString() ?: "",
                    score = when (val scoreValue = map["score"]) {
                        is Long -> scoreValue.toInt()
                        is Int -> scoreValue
                        is String -> scoreValue.toIntOrNull() ?: 0
                        else -> 0
                    },
                    timestamp = when (val timeValue = map["timestamp"]) {
                        is Long -> timeValue
                        is Int -> timeValue.toLong()
                        is String -> timeValue.toLongOrNull() ?: 0L
                        else -> 0L
                    },
                    location = locationMap?.let {
                        GameLocation(
                            district = it["district"]?.toString(),
                            city = it["city"]?.toString(),
                            country = it["country"]?.toString() ?: "Türkiye"
                        )
                    }
                )
            } catch (e: Exception) {
                PlayerProfile()
            }
        }
    }
}
