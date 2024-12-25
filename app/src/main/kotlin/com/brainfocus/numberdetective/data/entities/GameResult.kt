package com.brainfocus.numberdetective.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_results")
data class GameResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val score: Int,
    val attempts: Int,
    val timestamp: Long,
    val isWin: Boolean = true
)
