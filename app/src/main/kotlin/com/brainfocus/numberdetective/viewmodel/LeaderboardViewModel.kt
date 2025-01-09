package com.brainfocus.numberdetective.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.brainfocus.numberdetective.model.PlayerProfile
import com.brainfocus.numberdetective.repository.LeaderboardRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import android.util.Log

class LeaderboardViewModel(application: Application) : AndroidViewModel(application) {
    
    companion object {
        private const val TAG = "LeaderboardViewModel"
    }
    
    private val repository = LeaderboardRepository(application.applicationContext)
    private val viewModelJob = SupervisorJob()
    
    private val _leaderboardState = MutableStateFlow<LeaderboardState>(LeaderboardState.Loading)
    val leaderboardState: StateFlow<LeaderboardState> = _leaderboardState
    
    private val _playerStats = MutableStateFlow<PlayerProfile?>(null)
    val playerStats: StateFlow<PlayerProfile?> = _playerStats

    fun clearCoroutines() {
        viewModelJob.cancelChildren()
    }
    
    fun loadLeaderboard() {
        viewModelScope.launch(viewModelJob) {
            try {
                if (!repository.hasLocationPermission()) {
                    _leaderboardState.value = LeaderboardState.LocationPermissionRequired
                    return@launch
                }
                
                repository.getTopPlayers().collect { players ->
                    _leaderboardState.value = if (players.isEmpty()) {
                        LeaderboardState.Empty
                    } else {
                        LeaderboardState.Success(players)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading leaderboard: ${e.message}")
                if (e is kotlinx.coroutines.CancellationException) {
                    // İşlem iptal edildi, normal durum
                    return@launch
                }
                _leaderboardState.value = LeaderboardState.Error("Sıralama yüklenirken bir hata oluştu")
            }
        }
    }
    
    fun updateScore(account: GoogleSignInAccount, score: Int) {
        viewModelScope.launch(viewModelJob) {
            try {
                repository.updatePlayerScore(account, score)
                loadPlayerStats(account)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    // İşlem iptal edildi, normal durum
                    return@launch
                }
                Log.e(TAG, "Error updating score: ${e.message}")
            }
        }
    }
    
    fun loadPlayerStats(account: GoogleSignInAccount) {
        viewModelScope.launch(viewModelJob) {
            try {
                _playerStats.value = repository.getPlayerStats(account)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    // İşlem iptal edildi, normal durum
                    return@launch
                }
                Log.e(TAG, "Error loading player stats: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
    
    sealed class LeaderboardState {
        object Loading : LeaderboardState()
        object Empty : LeaderboardState()
        object LocationPermissionRequired : LeaderboardState()
        data class Success(val players: List<PlayerProfile>) : LeaderboardState()
        data class Error(val message: String) : LeaderboardState()
    }
}
