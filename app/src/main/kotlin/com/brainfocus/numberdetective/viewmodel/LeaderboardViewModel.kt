package com.brainfocus.numberdetective.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.model.PlayerScore
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.LeaderboardsClient
import com.google.android.gms.games.leaderboard.LeaderboardScore
import com.google.android.gms.games.leaderboard.LeaderboardVariant
import com.google.android.gms.tasks.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "LeaderboardViewModel"
        private const val MAX_SCORES = 25
    }

    private val _scores = MutableStateFlow<List<PlayerScore>>(emptyList())
    val scores: StateFlow<List<PlayerScore>> = _scores

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    suspend fun loadGlobalLeaderboard(activity: Activity) {
        _loading.value = true
        _error.value = null

        try {
            val leaderboardsClient = PlayGames.getLeaderboardsClient(activity)
            val leaderboardScores = leaderboardsClient.loadTopScores(
                activity.getString(R.string.leaderboard_global_all_time),
                LeaderboardVariant.TIME_SPAN_ALL_TIME,
                LeaderboardVariant.COLLECTION_PUBLIC,
                MAX_SCORES
            ).await()

            val scores = leaderboardScores.get()?.scores
            val playerScores = scores?.map { score ->
                PlayerScore(
                    id = score.scoreHolderDisplayName,
                    name = score.scoreHolderDisplayName,
                    score = score.rawScore,
                    rank = score.rank,
                    playerIcon = score.scoreHolderIconImageUri
                )
            } ?: emptyList()

            _scores.value = playerScores
        } catch (e: Exception) {
            Log.e(TAG, "Error loading global leaderboard", e)
            _error.value = e.message
        } finally {
            _loading.value = false
        }
    }


    fun submitScore(activity: Activity, score: Long) {
        viewModelScope.launch {
            try {
                val leaderboardsClient = PlayGames.getLeaderboardsClient(activity)
                // Submit to global leaderboard
                leaderboardsClient.submitScoreImmediate(
                    activity.getString(R.string.leaderboard_global_all_time),
                    score
                ).await()
            
                
                loadGlobalLeaderboard(activity) // Refresh global leaderboard after submitting new score
            } catch (e: Exception) {
                Log.e(TAG, "Error submitting score", e)
                _error.value = e.message
            }
        }
    }
}
