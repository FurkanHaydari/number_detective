package com.brainfocus.numberdetective.model

data class PlayerStats(
    val totalGames: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val highScore: Int = 0,
    val averageScore: Double = 0.0,
    val totalScore: Int = 0,
    val lastPlayed: Long = 0
)
