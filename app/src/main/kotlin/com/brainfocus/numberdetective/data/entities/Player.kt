package com.brainfocus.numberdetective.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class Player(
    @PrimaryKey
    val id: String,
    val name: String,
    val score: Int,
    val attempts: Int,
    val timestamp: Long
)
