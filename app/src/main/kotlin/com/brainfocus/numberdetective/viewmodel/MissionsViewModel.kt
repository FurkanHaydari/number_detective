package com.brainfocus.numberdetective.viewmodel

import com.brainfocus.numberdetective.base.BaseViewModel
import com.brainfocus.numberdetective.missions.DailyMission
import com.brainfocus.numberdetective.missions.MissionManager
import com.brainfocus.numberdetective.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MissionsViewModel(
    private val missionManager: MissionManager,
    private val preferencesManager: PreferencesManager
) : BaseViewModel() {

    private val _missionsState = MutableStateFlow<MissionsState>(MissionsState.Loading)
    val missionsState: StateFlow<MissionsState> = _missionsState

    private val _resetTimeState = MutableStateFlow<String>("")
    val resetTimeState: StateFlow<String> = _resetTimeState

    init {
        loadMissions()
        updateResetTime()
    }

    fun loadMissions() {
        launchIO {
            _missionsState.emit(MissionsState.Loading)
            
            try {
                val missions = missionManager.getDailyMissions()
                _missionsState.emit(MissionsState.Success(missions))
            } catch (e: Exception) {
                _missionsState.emit(MissionsState.Error("Görevler yüklenirken hata oluştu"))
            }
        }
    }

    fun claimReward(mission: DailyMission) {
        launchIO {
            try {
                missionManager.claimReward(mission.id)?.let { reward ->
                    // Ödülü kullanıcının puanına ekle
                    val currentScore = preferencesManager.getHighScore()
                    preferencesManager.updateHighScore(currentScore + reward)
                    
                    // Görevleri yeniden yükle
                    loadMissions()
                }
            } catch (e: Exception) {
                _error.emit(ErrorHandler.AppError.UnknownError("Ödül alınırken hata oluştu", e))
            }
        }
    }

    private fun updateResetTime() {
        launchMain {
            val calendar = java.util.Calendar.getInstance()
            val nextDay = calendar.apply {
                add(java.util.Calendar.DAY_OF_MONTH, 1)
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
            }
            
            val currentTime = System.currentTimeMillis()
            val timeUntilReset = nextDay.timeInMillis - currentTime
            
            val hoursLeft = timeUntilReset / (1000 * 60 * 60)
            val minutesLeft = (timeUntilReset % (1000 * 60 * 60)) / (1000 * 60)
            
            _resetTimeState.emit("Yeni görevlere kalan süre: ${hoursLeft}s ${minutesLeft}d")
        }
    }

    sealed class MissionsState {
        object Loading : MissionsState()
        data class Success(val missions: List<DailyMission>) : MissionsState()
        data class Error(val message: String) : MissionsState()
    }
}
