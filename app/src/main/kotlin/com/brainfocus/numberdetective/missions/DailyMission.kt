package com.brainfocus.numberdetective.missions

data class DailyMission(
    val id: String,
    val title: String,
    val description: String,
    val type: MissionType,
    val target: Int,
    val reward: Int,
    var progress: Int = 0,
    var isCompleted: Boolean = false,
    var isClaimed: Boolean = false
)

enum class MissionType {
    WIN_GAMES,           // Belirli sayıda oyun kazanma
    PLAY_GAMES,         // Belirli sayıda oyun oynama
    ACHIEVE_SCORE,      // Belirli bir skora ulaşma
    PERFECT_WINS,       // İlk denemede doğru tahmin
    QUICK_WINS,         // Belirli bir süreden önce kazanma
    DAILY_LOGIN         // Günlük giriş
}
