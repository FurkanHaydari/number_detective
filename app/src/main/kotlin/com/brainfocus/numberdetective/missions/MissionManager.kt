package com.brainfocus.numberdetective.missions

import android.content.Context
import com.brainfocus.numberdetective.utils.PreferencesManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class MissionManager private constructor(context: Context) {
    private val prefsManager = PreferencesManager.getInstance(context)
    private val gson = Gson()
    private var dailyMissions = mutableListOf<DailyMission>()
    private var lastUpdateDate: Long = 0

    companion object {
        private const val PREFS_MISSIONS = "daily_missions"
        private const val PREFS_LAST_UPDATE = "last_mission_update"

        @Volatile
        private var instance: MissionManager? = null

        fun getInstance(context: Context): MissionManager {
            return instance ?: synchronized(this) {
                instance ?: MissionManager(context).also { instance = it }
            }
        }
    }

    init {
        loadMissions()
        checkAndUpdateDailyMissions()
    }

    private fun loadMissions() {
        val missionsJson = prefsManager.getString(PREFS_MISSIONS, "")
        lastUpdateDate = prefsManager.getLong(PREFS_LAST_UPDATE, 0)

        if (missionsJson.isNotEmpty()) {
            val type = object : TypeToken<List<DailyMission>>() {}.type
            dailyMissions = gson.fromJson(missionsJson, type)
        }
    }

    private fun saveMissions() {
        val missionsJson = gson.toJson(dailyMissions)
        prefsManager.putString(PREFS_MISSIONS, missionsJson)
        prefsManager.putLong(PREFS_LAST_UPDATE, lastUpdateDate)
    }

    private fun checkAndUpdateDailyMissions() {
        val currentDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        if (currentDate > lastUpdateDate) {
            generateNewDailyMissions()
            lastUpdateDate = currentDate
            saveMissions()
        }
    }

    private fun generateNewDailyMissions() {
        dailyMissions.clear()
        dailyMissions.addAll(listOf(
            DailyMission(
                id = UUID.randomUUID().toString(),
                title = "Günün Şampiyonu",
                description = "3 oyun kazan",
                type = MissionType.WIN_GAMES,
                target = 3,
                reward = 500
            ),
            DailyMission(
                id = UUID.randomUUID().toString(),
                title = "Hızlı Düşünür",
                description = "30 saniyeden kısa sürede bir oyun kazan",
                type = MissionType.QUICK_WINS,
                target = 1,
                reward = 300
            ),
            DailyMission(
                id = UUID.randomUUID().toString(),
                title = "Keskin Zeka",
                description = "İlk denemede doğru tahmin yap",
                type = MissionType.PERFECT_WINS,
                target = 1,
                reward = 1000
            )
        ))
    }

    fun updateMissionProgress(type: MissionType, value: Int = 1, additionalData: Map<String, Any>? = null) {
        dailyMissions.filter { it.type == type && !it.isCompleted }.forEach { mission ->
            when (type) {
                MissionType.QUICK_WINS -> {
                    val timeSeconds = additionalData?.get("timeSeconds") as? Long ?: return
                    if (timeSeconds <= 30) {
                        mission.progress += value
                    }
                }
                MissionType.PERFECT_WINS -> {
                    val attempts = additionalData?.get("attempts") as? Int ?: return
                    if (attempts == 1) {
                        mission.progress += value
                    }
                }
                else -> mission.progress += value
            }

            if (mission.progress >= mission.target) {
                mission.isCompleted = true
            }
        }
        saveMissions()
    }

    fun claimReward(missionId: String): Int? {
        val mission = dailyMissions.find { it.id == missionId }
        return if (mission != null && mission.isCompleted && !mission.isClaimed) {
            mission.isClaimed = true
            saveMissions()
            mission.reward
        } else null
    }

    fun getDailyMissions(): List<DailyMission> {
        checkAndUpdateDailyMissions()
        return dailyMissions
    }
}
