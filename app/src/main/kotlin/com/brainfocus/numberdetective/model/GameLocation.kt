package com.brainfocus.numberdetective.model

data class GameLocation(
    val district: String? = null,
    val city: String? = null,
    val country: String = "Türkiye"
) {
    override fun toString(): String {
        return when {
            district != null -> district
            city != null -> city
            else -> country
        }
    }
}
