package com.brainfocus.numberdetective.data.repository

import com.brainfocus.numberdetective.data.dao.GameResultDao
import com.brainfocus.numberdetective.data.entities.GameResult
import com.brainfocus.numberdetective.utils.ErrorHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GameRepository(
    private val gameResultDao: GameResultDao,
    private val errorHandler: ErrorHandler
) {
    suspend fun saveGameResult(result: GameResult) {
        try {
            gameResultDao.insert(result)
        } catch (e: Exception) {
            errorHandler.handleError(
                ErrorHandler.AppError.DatabaseError(
                    "Error saving game result",
                    e
                ),
                null
            )
        }
    }

    fun getGameResults(): Flow<List<GameResult>> = flow {
        gameResultDao.getAllResults()
            .catch { e ->
                errorHandler.handleError(
                    ErrorHandler.AppError.DatabaseError(
                        "Error fetching game results",
                        e
                    ),
                    null
                )
            }
            .collect { emit(it) }
    }

    fun getHighScores(limit: Int = 10): Flow<List<GameResult>> = flow {
        gameResultDao.getTopScores(limit)
            .catch { e ->
                errorHandler.handleError(
                    ErrorHandler.AppError.DatabaseError(
                        "Error fetching high scores",
                        e
                    ),
                    null
                )
            }
            .collect { emit(it) }
    }

    suspend fun getPlayerStats(playerId: String): PlayerStats {
        return try {
            val totalGames = gameResultDao.getPlayerGameCount(playerId)
            val wins = gameResultDao.getPlayerWinCount(playerId)
            val bestScore = gameResultDao.getPlayerBestScore(playerId)
            val averageScore = gameResultDao.getPlayerAverageScore(playerId)

            PlayerStats(
                totalGames = totalGames,
                wins = wins,
                bestScore = bestScore,
                averageScore = averageScore
            )
        } catch (e: Exception) {
            errorHandler.handleError(
                ErrorHandler.AppError.DatabaseError(
                    "Error fetching player stats",
                    e
                ),
                null
            )
            PlayerStats() // Boş istatistikler döndür
        }
    }

    suspend fun clearOldResults(daysToKeep: Int = 30) {
        try {
            gameResultDao.deleteOldResults(daysToKeep)
        } catch (e: Exception) {
            errorHandler.handleError(
                ErrorHandler.AppError.DatabaseError(
                    "Error clearing old results",
                    e
                ),
                null
            )
        }
    }

    data class PlayerStats(
        val totalGames: Int = 0,
        val wins: Int = 0,
        val bestScore: Int = 0,
        val averageScore: Double = 0.0
    )
}
