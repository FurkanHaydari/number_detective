package com.brainfocus.numberdetective.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missions")
data class Mission(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val reward: Int,
    val isCompleted: Boolean,
    val isRewardClaimed: Boolean,
    val expiryDate: Long
)
