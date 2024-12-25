package com.brainfocus.numberdetective.viewmodel

import com.brainfocus.numberdetective.base.BaseViewModel
import com.brainfocus.numberdetective.data.entities.GameResult
import com.brainfocus.numberdetective.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LeaderboardViewModel(
    private val gameRepository: GameRepository
) : BaseViewModel() {

    private val _leaderboardState = MutableStateFlow<LeaderboardState>(LeaderboardState.Loading)
    val leaderboardState: StateFlow<LeaderboardState> = _leaderboardState

    private val _sortType = MutableStateFlow(SortType.SCORE)
    val sortType: StateFlow<SortType> = _sortType

    init {
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        launchIO {
            _leaderboardState.emit(LeaderboardState.Loading)
            
            gameRepository.getHighScores()
                .collect { results ->
                    val sortedResults = when (_sortType.value) {
                        SortType.SCORE -> results.sortedByDescending { it.score }
                        SortType.WINS -> results.sortedByDescending { it.isWin }
                        SortType.TIME -> results.sortedBy { it.timeTaken }
                    }
                    
                    _leaderboardState.emit(LeaderboardState.Success(sortedResults))
                }
        }
    }

    fun setSortType(type: SortType) {
        launchMain {
            _sortType.emit(type)
            loadLeaderboard()
        }
    }

    sealed class LeaderboardState {
        object Loading : LeaderboardState()
        data class Success(val results: List<GameResult>) : LeaderboardState()
        data class Error(val message: String) : LeaderboardState()
    }

    enum class SortType {
        SCORE,
        WINS,
        TIME
    }
}
