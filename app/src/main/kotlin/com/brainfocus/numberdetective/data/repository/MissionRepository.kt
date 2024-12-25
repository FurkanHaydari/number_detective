package com.brainfocus.numberdetective.data.repository

import com.brainfocus.numberdetective.data.dao.MissionDao
import com.brainfocus.numberdetective.data.entities.Mission
import com.brainfocus.numberdetective.utils.ErrorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class MissionRepository(
    private val missionDao: MissionDao,
    private val errorHandler: ErrorHandler
) {
    fun getActiveMissions(): Flow<List<Mission>> {
        return missionDao.getActiveMissions(System.currentTimeMillis())
            .catch { e ->
                errorHandler.handleError(
                    ErrorHandler.AppError.DatabaseError("Aktif görevler yüklenirken hata oluştu", e),
                    null
                )
            }
            .flowOn(Dispatchers.IO)
    }

    fun getUnclaimedRewards(): Flow<List<Mission>> {
        return missionDao.getCompletedMissionsWithUnclaimedRewards()
            .catch { e ->
                errorHandler.handleError(
                    ErrorHandler.AppError.DatabaseError("Alınmamış ödüller yüklenirken hata oluştu", e),
                    null
                )
            }
            .flowOn(Dispatchers.IO)
    }

    fun getTotalClaimedRewards(): Flow<Int> {
        return missionDao.getTotalClaimedRewards()
            .catch { e ->
                errorHandler.handleError(
                    ErrorHandler.AppError.DatabaseError("Toplam ödül miktarı yüklenirken hata oluştu", e),
                    null
                )
            }
            .flowOn(Dispatchers.IO)
    }

    suspend fun addMission(mission: Mission) {
        withContext(Dispatchers.IO) {
            try {
                missionDao.insertMission(mission)
            } catch (e: Exception) {
                errorHandler.handleError(
                    ErrorHandler.AppError.DatabaseError("Görev eklenirken hata oluştu", e),
                    null
                )
            }
        }
    }

    suspend fun updateMission(mission: Mission) {
        withContext(Dispatchers.IO) {
            try {
                missionDao.updateMission(mission)
            } catch (e: Exception) {
                errorHandler.handleError(
                    ErrorHandler.AppError.DatabaseError("Görev güncellenirken hata oluştu", e),
                    null
                )
            }
        }
    }

    suspend fun completeMission(missionId: String) {
        withContext(Dispatchers.IO) {
            try {
                missionDao.completeMission(missionId)
            } catch (e: Exception) {
                errorHandler.handleError(
                    ErrorHandler.AppError.DatabaseError("Görev tamamlanırken hata oluştu", e),
                    null
                )
            }
        }
    }

    suspend fun claimReward(missionId: String) {
        withContext(Dispatchers.IO) {
            try {
                missionDao.claimMissionReward(missionId)
            } catch (e: Exception) {
                errorHandler.handleError(
                    ErrorHandler.AppError.DatabaseError("Ödül alınırken hata oluştu", e),
                    null
                )
            }
        }
    }

    suspend fun cleanupExpiredMissions() {
        withContext(Dispatchers.IO) {
            try {
                missionDao.deleteExpiredMissions(System.currentTimeMillis())
            } catch (e: Exception) {
                errorHandler.handleError(
                    ErrorHandler.AppError.DatabaseError("Süresi geçmiş görevler temizlenirken hata oluştu", e),
                    null
                )
            }
        }
    }

    suspend fun clearAllMissions() {
        withContext(Dispatchers.IO) {
            try {
                missionDao.deleteAllMissions()
            } catch (e: Exception) {
                errorHandler.handleError(
                    ErrorHandler.AppError.DatabaseError("Görevler temizlenirken hata oluştu", e),
                    null
                )
            }
        }
    }
}
