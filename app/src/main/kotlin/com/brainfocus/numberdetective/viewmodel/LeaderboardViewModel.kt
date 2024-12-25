package com.brainfocus.numberdetective.viewmodel

import com.brainfocus.numberdetective.base.BaseViewModel
import com.brainfocus.numberdetective.data.entities.Player
import com.brainfocus.numberdetective.data.repository.GameRepository
import com.brainfocus.numberdetective.utils.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LeaderboardViewModel(
    private val gameRepository: GameRepository,
    errorHandler: ErrorHandler
) : BaseViewModel(errorHandler) {

    private val _leaderboardEntries = MutableStateFlow<List<Player>>(emptyList())
    val leaderboardEntries: StateFlow<List<Player>> = _leaderboardEntries

    init {
        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        launchWithErrorHandling {
            gameRepository.getHighScores().collect { results ->
                _leaderboardEntries.value = results.mapIndexed { index, result ->
                    Player(
                        id = result.id.toString(),
                        name = "Player ${index + 1}",
                        score = result.score,
                        attempts = result.attempts,
                        timestamp = result.timestamp
                    )
                }.sortedByDescending { it.score }
            }
        }
    }

    fun refreshLeaderboard() {
        loadLeaderboard()
    }
}
