package com.brainfocus.numberdetective.viewmodel

import com.brainfocus.numberdetective.base.BaseViewModel
import com.brainfocus.numberdetective.data.repository.GameRepository
import com.brainfocus.numberdetective.missions.MissionManager
import com.brainfocus.numberdetective.missions.MissionType
import com.brainfocus.numberdetective.utils.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

class GameViewModel(
    private val gameRepository: GameRepository,
    private val missionManager: MissionManager,
    errorHandler: ErrorHandler
) : BaseViewModel(errorHandler) {

    private val _gameState = MutableStateFlow<GameState>(GameState.Initial)
    val gameState: StateFlow<GameState> = _gameState

    private var targetNumber: Int = 0
    private var attempts: Int = 0
    private var score: Int = 0
    private val maxAttempts = 10

    init {
        startNewGame()
    }

    fun startNewGame() {
        targetNumber = Random.nextInt(1, 101)
        attempts = 0
        score = 1000
        _gameState.value = GameState.Initial
    }

    fun makeGuess(guessStr: String) {
        try {
            val guess = guessStr.toInt()
            if (guess !in 1..100) {
                _gameState.value = GameState.Playing(
                    hint = "Please enter a number between 1 and 100",
                    attempts = attempts,
                    maxAttempts = maxAttempts,
                    score = score
                )
                return
            }

            attempts++
            score = calculateScore(attempts)

            when {
                guess == targetNumber -> {
                    handleWin()
                }
                attempts >= maxAttempts -> {
                    handleLoss()
                }
                guess < targetNumber -> {
                    _gameState.value = GameState.Playing(
                        hint = "Higher!",
                        attempts = attempts,
                        maxAttempts = maxAttempts,
                        score = score
                    )
                }
                else -> {
                    _gameState.value = GameState.Playing(
                        hint = "Lower!",
                        attempts = attempts,
                        maxAttempts = maxAttempts,
                        score = score
                    )
                }
            }
        } catch (e: NumberFormatException) {
            _gameState.value = GameState.Playing(
                hint = "Please enter a valid number",
                attempts = attempts,
                maxAttempts = maxAttempts,
                score = score
            )
        }
    }

    private fun handleWin() {
        launchWithErrorHandling {
            gameRepository.saveGameResult(score, attempts)
            missionManager.updateMissionProgress(MissionType.WINS)
            if (score > 800) {
                missionManager.updateMissionProgress(MissionType.HIGH_SCORE)
            }
            _gameState.value = GameState.Won(score)
        }
    }

    private fun handleLoss() {
        launchWithErrorHandling {
            missionManager.updateMissionProgress(MissionType.GAMES_PLAYED)
            _gameState.value = GameState.Lost(targetNumber)
        }
    }

    private fun calculateScore(attempts: Int): Int {
        return maxOf(0, 1000 - (attempts - 1) * 100)
    }
}

sealed class GameState {
    object Initial : GameState()
    data class Playing(
        val hint: String,
        val attempts: Int,
        val maxAttempts: Int,
        val score: Int
    ) : GameState()
    data class Won(val score: Int) : GameState()
    data class Lost(val targetNumber: Int) : GameState()
}
