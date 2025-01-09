package com.brainfocus.numberdetective.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brainfocus.numberdetective.database.LeaderboardDatabase
import com.brainfocus.numberdetective.model.GameLocation
import com.brainfocus.numberdetective.model.PlayerProfile
import com.brainfocus.numberdetective.model.PlayerStats
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class LeaderboardViewModel : ViewModel() {
    private val leaderboardDatabase = LeaderboardDatabase()
    
    private val _leaderboardData = MutableLiveData<List<PlayerProfile>>()
    val leaderboardData: LiveData<List<PlayerProfile>> = _leaderboardData

    private val _playerStats = MutableLiveData<PlayerStats>()
    val playerStats: LiveData<PlayerStats> = _playerStats

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadLeaderboard(location: GameLocation) {
        viewModelScope.launch {
            try {
                leaderboardDatabase.getLeaderboard(location)
                    .catch { e ->
                        Log.e(TAG, "Error loading leaderboard: ${e.message}")
                        _error.value = "Liderlik tablosu yüklenirken hata oluştu"
                    }
                    .collect { players ->
                        _leaderboardData.value = players
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in loadLeaderboard: ${e.message}")
                _error.value = "Liderlik tablosu yüklenirken hata oluştu"
            }
        }
    }

    fun updatePlayerScore(userId: String, score: Int) {
        viewModelScope.launch {
            try {
                leaderboardDatabase.updatePlayerScore(
                    userId = userId,
                    score = score,
                    location = GameLocation()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error updating player score: ${e.message}")
                _error.value = "Skor güncellenirken hata oluştu"
            }
        }
    }

    fun loadPlayerStats(userId: String) {
        viewModelScope.launch {
            try {
                leaderboardDatabase.getPlayerStats(userId)
                    .catch { e ->
                        Log.e(TAG, "Error loading player stats: ${e.message}")
                        _error.value = "Oyuncu istatistikleri yüklenirken hata oluştu"
                    }
                    .collect { stats ->
                        _playerStats.value = stats
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in loadPlayerStats: ${e.message}")
                _error.value = "Oyuncu istatistikleri yüklenirken hata oluştu"
            }
        }
    }

    companion object {
        private const val TAG = "LeaderboardViewModel"
    }
}
