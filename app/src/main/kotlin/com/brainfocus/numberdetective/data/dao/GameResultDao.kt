package com.brainfocus.numberdetective.data.dao

import androidx.room.*
import com.brainfocus.numberdetective.data.entities.GameResult
import kotlinx.coroutines.flow.Flow

@Dao
interface GameResultDao {
    @Query("SELECT * FROM game_results ORDER BY date DESC")
    fun getAllGameResults(): Flow<List<GameResult>>

    @Query("SELECT * FROM game_results WHERE isWin = 1 ORDER BY score DESC LIMIT :limit")
    fun getTopScores(limit: Int = 10): Flow<List<GameResult>>

    @Query("SELECT * FROM game_results WHERE playerId = :playerId ORDER BY date DESC")
    fun getPlayerGameResults(playerId: String): Flow<List<GameResult>>

    @Query("SELECT COUNT(*) FROM game_results WHERE isWin = 1")
    fun getTotalWins(): Flow<Int>

    @Query("SELECT AVG(timeTaken) FROM game_results WHERE isWin = 1")
    fun getAverageWinTime(): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameResult(gameResult: GameResult)

    @Delete
    suspend fun deleteGameResult(gameResult: GameResult)

    @Query("DELETE FROM game_results")
    suspend fun deleteAllGameResults()
}
