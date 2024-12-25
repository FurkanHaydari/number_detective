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

    suspend fun saveGameResult(score: Int, attempts: Int) {
        try {
            val gameResult = GameResult(
                score = score,
                attempts = attempts,
                timestamp = System.currentTimeMillis()
            )
            gameResultDao.insert(gameResult)
        } catch (e: Exception) {
            errorHandler.handleError(e)
            throw e
        }
    }

    fun getHighScores(): Flow<List<GameResult>> = flow {
        try {
            gameResultDao.getHighScores().collect { results ->
                emit(results)
            }
        } catch (e: Exception) {
            errorHandler.handleError(e)
            emit(emptyList())
        }
    }

    suspend fun getGameResult(id: Long): GameResult? {
        return try {
            gameResultDao.getById(id)
        } catch (e: Exception) {
            errorHandler.handleError(e)
            null
        }
    }

    suspend fun updateGameResult(gameResult: GameResult) {
        try {
            gameResultDao.update(gameResult)
        } catch (e: Exception) {
            errorHandler.handleError(e)
            throw e
        }
    }

    suspend fun deleteGameResult(gameResult: GameResult) {
        try {
            gameResultDao.delete(gameResult)
        } catch (e: Exception) {
            errorHandler.handleError(e)
            throw e
        }
    }
}
