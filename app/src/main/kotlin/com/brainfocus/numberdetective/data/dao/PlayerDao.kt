package com.brainfocus.numberdetective.data.dao

import androidx.room.*
import com.brainfocus.numberdetective.data.entities.Player
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players")
    fun getAllPlayers(): Flow<List<Player>>
    
    @Query("SELECT * FROM players WHERE id = :playerId")
    suspend fun getPlayerById(playerId: String): Player?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: Player)
    
    @Update
    suspend fun updatePlayer(player: Player)
    
    @Delete
    suspend fun deletePlayer(player: Player)
    
    @Query("UPDATE players SET totalScore = totalScore + :score WHERE id = :playerId")
    suspend fun updatePlayerScore(playerId: String, score: Int)
    
    @Query("UPDATE players SET gamesPlayed = gamesPlayed + 1, gamesWon = gamesWon + :wonGame WHERE id = :playerId")
    suspend fun updatePlayerStats(playerId: String, wonGame: Int)
    
    @Query("UPDATE players SET bestTime = CASE WHEN bestTime IS NULL OR :newTime < bestTime THEN :newTime ELSE bestTime END WHERE id = :playerId")
    suspend fun updateBestTime(playerId: String, newTime: Long)
}
