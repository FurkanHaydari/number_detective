package com.brainfocus.numberdetective.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_results")
data class GameResult(
    @PrimaryKey
    val id: String,
    val playerId: String,
    val score: Int,
    val attempts: Int,
    val isWin: Boolean,
    val timeTaken: Long,
    val date: Long
)
