package com.brainfocus.numberdetective.data.repository

import com.brainfocus.numberdetective.data.dao.MissionDao
import com.brainfocus.numberdetective.missions.DailyMission
import com.brainfocus.numberdetective.utils.ErrorHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class MissionRepository(
    private val missionDao: MissionDao,
    private val errorHandler: ErrorHandler
) {

    fun getMissions(): Flow<List<DailyMission>> = flow {
        try {
            missionDao.getAllMissions().collect { missions ->
                emit(missions.map { mission ->
                    DailyMission(
                        id = mission.id,
                        title = mission.title,
                        description = mission.description,
                        type = mission.type,
                        targetProgress = mission.targetProgress,
                        reward = mission.reward,
                        currentProgress = mission.currentProgress,
                        isCompleted = mission.isCompleted,
                        isRewardClaimed = mission.isRewardClaimed
                    )
                })
            }
        } catch (e: Exception) {
            errorHandler.handleError(e)
            emit(emptyList())
        }
    }.catch { e -> 
        errorHandler.handleError(e)
        emit(emptyList())
    }

    suspend fun saveMissions(missions: List<DailyMission>) {
        try {
            missionDao.clearMissions()
            missions.forEach { mission ->
                missionDao.insert(mission.toEntity())
            }
        } catch (e: Exception) {
            errorHandler.handleError(e)
            throw e
        }
    }

    private fun DailyMission.toEntity() = com.brainfocus.numberdetective.data.entities.Mission(
        id = id,
        title = title,
        description = description,
        type = type,
        targetProgress = targetProgress,
        reward = reward,
        currentProgress = currentProgress,
        isCompleted = isCompleted,
        isRewardClaimed = isRewardClaimed
    )
}
