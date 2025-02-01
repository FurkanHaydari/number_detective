package com.brainfocus.numberdetective.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brainfocus.numberdetective.model.PlayerScore
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.games.Games
import com.google.android.gms.games.leaderboard.LeaderboardVariant
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

    private val _scores = MutableStateFlow<List<PlayerScore>>(emptyList())
    val scores: StateFlow<List<PlayerScore>> = _scores

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    companion object {
        private const val TAG = "LeaderboardViewModel"
        private const val LEADERBOARD_ID = "CgkIxZWJ8KYWEAIQAQ"
        private const val MAX_SCORES = 25
    }

    fun loadLeaderboard(activity: android.app.Activity) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val account = GoogleSignIn.getLastSignedInAccount(activity)
                if (account != null) {
                    val leaderboardsClient = Games.getLeaderboardsClient(activity, account)
                    val leaderboardScores = leaderboardsClient.loadTopScores(
                        LEADERBOARD_ID,
                        LeaderboardVariant.TIME_SPAN_ALL_TIME,
                        LeaderboardVariant.COLLECTION_PUBLIC,
                        MAX_SCORES
                    ).await()

                    val scores = leaderboardScores.get()?.scores?.map { score ->
                        PlayerScore(
                            id = score.scoreHolderDisplayName,
                            name = score.scoreHolderDisplayName,
                            score = score.rawScore,
                            rank = score.rank
                        )
                    } ?: emptyList()
                    
                    _scores.value = scores
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading leaderboard: ${e.message}")
                _error.value = "Liderlik tablosu yüklenirken bir hata oluştu"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitScore(activity: android.app.Activity, score: Long) {
        viewModelScope.launch {
            try {
                val account = GoogleSignIn.getLastSignedInAccount(activity)
                if (account != null) {
                    val leaderboardsClient = Games.getLeaderboardsClient(activity, account)
                    leaderboardsClient.submitScoreImmediate(LEADERBOARD_ID, score).await()
                    loadLeaderboard(activity) // Refresh leaderboard after submitting new score
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error submitting score: ${e.message}")
                _error.value = "Skor gönderilirken bir hata oluştu"
            }
        }
    }
}
