package com.brainfocus.numberdetective.model

data class LeaderboardEntry(
    val id: String,
    val playerName: String,
    val score: Int,
    val wins: Int,
    val gamesPlayed: Int,
    val timestamp: Long
)
