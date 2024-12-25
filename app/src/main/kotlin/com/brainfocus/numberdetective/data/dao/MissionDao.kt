package com.brainfocus.numberdetective.data.dao

import androidx.room.*
import com.brainfocus.numberdetective.data.entities.Mission
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionDao {
    @Query("SELECT * FROM missions")
    fun getAllMissions(): Flow<List<Mission>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mission: Mission)

    @Update
    suspend fun update(mission: Mission)

    @Delete
    suspend fun delete(mission: Mission)

    @Query("DELETE FROM missions")
    suspend fun clearMissions()

    @Query("SELECT * FROM missions WHERE id = :id")
    suspend fun getMissionById(id: String): Mission?
}
