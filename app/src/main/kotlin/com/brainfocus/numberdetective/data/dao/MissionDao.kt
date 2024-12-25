package com.brainfocus.numberdetective.data.dao

import androidx.room.*
import com.brainfocus.numberdetective.data.entities.Mission
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionDao {
    @Query("SELECT * FROM missions WHERE expiryDate > :currentTime ORDER BY expiryDate ASC")
    fun getActiveMissions(currentTime: Long): Flow<List<Mission>>

    @Query("SELECT * FROM missions WHERE isCompleted = 1 AND isRewardClaimed = 0")
    fun getCompletedMissionsWithUnclaimedRewards(): Flow<List<Mission>>

    @Query("SELECT SUM(reward) FROM missions WHERE isCompleted = 1 AND isRewardClaimed = 1")
    fun getTotalClaimedRewards(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: Mission)

    @Update
    suspend fun updateMission(mission: Mission)

    @Query("UPDATE missions SET isCompleted = 1 WHERE id = :missionId")
    suspend fun completeMission(missionId: String)

    @Query("UPDATE missions SET isRewardClaimed = 1 WHERE id = :missionId")
    suspend fun claimMissionReward(missionId: String)

    @Query("DELETE FROM missions WHERE expiryDate < :currentTime")
    suspend fun deleteExpiredMissions(currentTime: Long)

    @Query("DELETE FROM missions")
    suspend fun deleteAllMissions()
}
