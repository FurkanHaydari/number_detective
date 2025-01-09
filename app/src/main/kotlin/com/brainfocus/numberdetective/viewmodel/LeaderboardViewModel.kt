package com.brainfocus.numberdetective.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brainfocus.numberdetective.database.LeaderboardDatabase
import com.brainfocus.numberdetective.location.LocationManager
import com.brainfocus.numberdetective.model.GameLocation
import com.brainfocus.numberdetective.model.PlayerProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LeaderboardViewModel : ViewModel() {
    private val leaderboardDatabase = LeaderboardDatabase()
    private val locationManager = LocationManager()

    private val _leaderboardState = MutableStateFlow<LeaderboardState>(LeaderboardState.Loading)
    val leaderboardState: StateFlow<LeaderboardState> = _leaderboardState

    sealed class LeaderboardState {
        object Loading : LeaderboardState()
        data class Success(val players: List<PlayerProfile>, val location: GameLocation) : LeaderboardState()
        data class Error(val message: String) : LeaderboardState()
    }

    fun loadLeaderboard(context: Context) {
        viewModelScope.launch {
            _leaderboardState.value = LeaderboardState.Loading

            try {
                val location = locationManager.getCurrentLocation(context)
                if (location != null && !location.district.isNullOrEmpty()) {
                    val players = leaderboardDatabase.getDistrictLeaderboard(location.district)
                    if (players.isNotEmpty()) {
                        _leaderboardState.value = LeaderboardState.Success(players, location)
                    } else {
                        _leaderboardState.value = LeaderboardState.Error("Bu bölgede henüz sıralama verisi yok")
                    }
                } else {
                    _leaderboardState.value = LeaderboardState.Error("Konum bilgisi alınamadı")
                }
            } catch (e: Exception) {
                _leaderboardState.value = LeaderboardState.Error("Sıralama yüklenirken bir hata oluştu")
            }
        }
    }

    fun updatePlayerScore(userId: String, score: Int) {
        viewModelScope.launch {
            try {
                val currentPlayer = leaderboardDatabase.getPlayer(userId)
                val newScore = if (currentPlayer != null) {
                    maxOf(currentPlayer.score, score)
                } else {
                    score
                }
                
                leaderboardDatabase.updatePlayerScore(
                    userId = userId,
                    score = newScore,
                    location = GameLocation()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error updating player score: ${e.message}")
                _leaderboardState.value = LeaderboardState.Error("Skor güncellenirken hata oluştu")
            }
        }
    }

    companion object {
        private const val TAG = "LeaderboardViewModel"
    }
}
