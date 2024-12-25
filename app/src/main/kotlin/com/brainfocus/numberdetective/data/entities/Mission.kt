package com.brainfocus.numberdetective.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.brainfocus.numberdetective.missions.MissionType

@Entity(tableName = "missions")
data class Mission(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val type: MissionType,
    val targetProgress: Int,
    val reward: Int,
    val currentProgress: Int = 0,
    val isCompleted: Boolean = false,
    val isRewardClaimed: Boolean = false
)
