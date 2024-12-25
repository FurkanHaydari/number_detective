package com.brainfocus.numberdetective.missions

import com.brainfocus.numberdetective.missions.MissionType

data class DailyMission(
    val id: String,
    val title: String,
    val description: String,
    val type: MissionType,
    val targetProgress: Int,
    val reward: Int,
    val currentProgress: Int = 0,
    val isCompleted: Boolean = false,
    val isRewardClaimed: Boolean = false
) {
    fun isCompletable(): Boolean {
        return currentProgress >= targetProgress && !isCompleted
    }

    fun isRewardClaimable(): Boolean {
        return isCompleted && !isRewardClaimed
    }

    fun updateProgress(progress: Int): DailyMission {
        val newProgress = currentProgress + progress
        val completed = newProgress >= targetProgress
        return copy(
            currentProgress = newProgress,
            isCompleted = completed
        )
    }

    fun claimReward(): DailyMission {
        return if (isRewardClaimable()) {
            copy(isRewardClaimed = true)
        } else {
            this
        }
    }
}
