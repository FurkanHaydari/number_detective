package com.brainfocus.numberdetective.missions

import com.brainfocus.numberdetective.data.repository.MissionRepository
import com.brainfocus.numberdetective.utils.ErrorHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.util.*

class MissionManager(
    private val missionRepository: MissionRepository,
    private val errorHandler: ErrorHandler
) {
    fun getMissions(): Flow<List<DailyMission>> {
        return missionRepository.getMissions()
            .catch { e ->
                errorHandler.handleError(e)
                emit(emptyList())
            }
    }

    suspend fun generateDailyMissions() {
        try {
            val missions = listOf(
                DailyMission(
                    id = UUID.randomUUID().toString(),
                    title = "Win Games",
                    description = "Win 3 games today",
                    type = MissionType.WINS,
                    targetProgress = 3,
                    reward = 500
                ),
                DailyMission(
                    id = UUID.randomUUID().toString(),
                    title = "Play Games",
                    description = "Play 5 games today",
                    type = MissionType.GAMES_PLAYED,
                    targetProgress = 5,
                    reward = 300
                ),
                DailyMission(
                    id = UUID.randomUUID().toString(),
                    title = "High Score",
                    description = "Achieve a score of 800 or higher",
                    type = MissionType.HIGH_SCORE,
                    targetProgress = 1,
                    reward = 1000
                )
            )
            missionRepository.saveMissions(missions)
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }

    suspend fun updateMissionProgress(type: MissionType, progress: Int = 1) {
        try {
            val missions = missionRepository.getMissions()
                .map { missionList ->
                    missionList.map { mission ->
                        if (mission.type == type && !mission.isCompleted) {
                            mission.updateProgress(progress)
                        } else {
                            mission
                        }
                    }
                }
                .catch { e ->
                    errorHandler.handleError(e)
                    emit(emptyList())
                }

            missions.collect { updatedMissions ->
                missionRepository.saveMissions(updatedMissions)
            }
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }

    suspend fun claimReward(missionId: String): Boolean {
        return try {
            val missions = missionRepository.getMissions()
                .map { missionList ->
                    missionList.map { mission ->
                        if (mission.id == missionId && mission.isRewardClaimable()) {
                            mission.claimReward()
                        } else {
                            mission
                        }
                    }
                }
                .catch { e ->
                    errorHandler.handleError(e)
                    emit(emptyList())
                }

            var rewardClaimed = false
            missions.collect { updatedMissions ->
                missionRepository.saveMissions(updatedMissions)
                rewardClaimed = updatedMissions.any { it.id == missionId && it.isRewardClaimed }
            }
            rewardClaimed
        } catch (e: Exception) {
            errorHandler.handleError(e)
            false
        }
    }

    suspend fun resetDailyMissions() {
        try {
            generateDailyMissions()
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }

    companion object {
        fun create(missionRepository: MissionRepository, errorHandler: ErrorHandler): MissionManager {
            return MissionManager(missionRepository, errorHandler)
        }
    }
}
