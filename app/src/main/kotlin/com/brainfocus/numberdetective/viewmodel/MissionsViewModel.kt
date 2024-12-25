package com.brainfocus.numberdetective.viewmodel

import com.brainfocus.numberdetective.base.BaseViewModel
import com.brainfocus.numberdetective.missions.DailyMission
import com.brainfocus.numberdetective.missions.MissionManager
import com.brainfocus.numberdetective.utils.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MissionsViewModel(
    private val missionManager: MissionManager,
    errorHandler: ErrorHandler
) : BaseViewModel(errorHandler) {

    private val _missions = MutableStateFlow<List<DailyMission>>(emptyList())
    val missions: StateFlow<List<DailyMission>> = _missions

    init {
        loadMissions()
    }

    private fun loadMissions() {
        launchWithErrorHandling {
            missionManager.getMissions().collect { missionsList ->
                _missions.value = missionsList
            }
        }
    }

    fun claimReward(missionId: String) {
        launchWithErrorHandling {
            val success = missionManager.claimReward(missionId)
            if (success) {
                loadMissions()
            }
        }
    }

    fun refreshMissions() {
        launchWithErrorHandling {
            missionManager.resetDailyMissions()
            loadMissions()
        }
    }
}
