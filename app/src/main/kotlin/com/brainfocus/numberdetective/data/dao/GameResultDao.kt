package com.brainfocus.numberdetective.data.dao

import androidx.room.*
import com.brainfocus.numberdetective.data.entities.GameResult
import kotlinx.coroutines.flow.Flow

@Dao
interface GameResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gameResult: GameResult)

    @Update
    suspend fun update(gameResult: GameResult)

    @Delete
    suspend fun delete(gameResult: GameResult)

    @Query("SELECT * FROM game_results WHERE id = :id")
    suspend fun getById(id: Long): GameResult?

    @Query("SELECT * FROM game_results ORDER BY score DESC LIMIT 10")
    fun getHighScores(): Flow<List<GameResult>>

    @Query("DELETE FROM game_results")
    suspend fun deleteAll()
}
