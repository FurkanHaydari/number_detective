package com.brainfocus.numberdetective.data.model

import com.google.android.gms.games.leaderboard.LeaderboardScore

sealed class LeaderboardState {
    object Loading : LeaderboardState()
    data class Success(val scores: List<LeaderboardScore>) : LeaderboardState()
    data class Error(val message: String) : LeaderboardState()
}
