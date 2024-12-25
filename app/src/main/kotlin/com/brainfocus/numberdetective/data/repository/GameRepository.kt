package com.brainfocus.numberdetective.data.repository

import com.brainfocus.numberdetective.data.dao.GameResultDao
import com.brainfocus.numberdetective.data.entities.GameResult
import com.brainfocus.numberdetective.utils.ErrorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class GameRepository(
    private val gameResultDao: GameResultDao,
    private val errorHandler: ErrorHandler
) {
    fun getHighScores(limit: Int = 10): Flow<List<GameResult>> {
        return gameResultDao.getTopScores(limit)
            .catch { e ->
                errorHandler.handleError(
                    ErrorHandler.AppError.DatabaseError("Yüksek skorlar yüklenirken hata oluştu", e),
                    null
                )
            }
            .flowOn(Dispatchers.IO)
    }

    fun getPlayerGameResults(playerId: String): Flow<List<GameResult>> {
        return gameResultDao.getPlayerGameResults(playerId)
            .catch { e ->
                errorHandler.handleError(
                    ErrorHandler.AppError.DatabaseError("Oyun sonuçları yüklenirken hata oluştu", e),
                    null
                )
            }
            .flowOn(Dispatchers.IO)
    }

    fun getTotalWins(): Flow<Int> {
        return gameResultDao.getTotalWins()
            .catch { e ->
                errorHandler.handleError(
                    ErrorHandler.AppError.DatabaseError("Toplam kazanma sayısı yüklenirken hata oluştu", e),
                    null
                )
            }
            .flowOn(Dispatchers.IO)
    }

    fun getAverageWinTime(): Flow<Long> {
        return gameResultDao.getAverageWinTime()
            .catch { e ->
                errorHandler.handleError(
                    ErrorHandler.AppError.DatabaseError("Ortalama kazanma süresi yüklenirken hata oluştu", e),
                    null
                )
            }
            .flowOn(Dispatchers.IO)
    }

    suspend fun saveGameResult(gameResult: GameResult) {
        withContext(Dispatchers.IO) {
            try {
                gameResultDao.insertGameResult(gameResult)
            } catch (e: Exception) {
                errorHandler.handleError(
                    ErrorHandler.AppError.DatabaseError("Oyun sonucu kaydedilirken hata oluştu", e),
                    null
                )
            }
        }
    }

    suspend fun deleteGameResult(gameResult: GameResult) {
        withContext(Dispatchers.IO) {
            try {
                gameResultDao.deleteGameResult(gameResult)
            } catch (e: Exception) {
                errorHandler.handleError(
                    ErrorHandler.AppError.DatabaseError("Oyun sonucu silinirken hata oluştu", e),
                    null
                )
            }
        }
    }

    suspend fun clearAllGameResults() {
        withContext(Dispatchers.IO) {
            try {
                gameResultDao.deleteAllGameResults()
            } catch (e: Exception) {
                errorHandler.handleError(
                    ErrorHandler.AppError.DatabaseError("Oyun sonuçları temizlenirken hata oluştu", e),
                    null
                )
            }
        }
    }
}
