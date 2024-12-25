package com.brainfocus.numberdetective.viewmodel

import androidx.lifecycle.viewModelScope
import com.brainfocus.numberdetective.base.BaseViewModel
import com.brainfocus.numberdetective.data.entities.GameResult
import com.brainfocus.numberdetective.data.repository.GameRepository
import com.brainfocus.numberdetective.game.NumberDetectiveGame
import com.brainfocus.numberdetective.utils.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class GameViewModel(
    private val gameRepository: GameRepository,
    private val errorHandler: ErrorHandler
) : BaseViewModel() {

    private val game = NumberDetectiveGame()
    
    private val _gameState = MutableStateFlow<GameState>(GameState.Initial)
    val gameState: StateFlow<GameState> = _gameState

    private val _score = MutableStateFlow(1000)
    val score: StateFlow<Int> = _score

    private val _attempts = MutableStateFlow(3)
    val attempts: StateFlow<Int> = _attempts

    private val _guessHistory = MutableStateFlow<List<GuessResult>>(emptyList())
    val guessHistory: StateFlow<List<GuessResult>> = _guessHistory

    private var startTime: Long = 0

    init {
        startNewGame()
    }

    fun startNewGame() {
        viewModelScope.launch {
            game.startNewGame()
            _gameState.emit(GameState.Playing)
            _score.emit(1000)
            _attempts.emit(3)
            _guessHistory.emit(emptyList())
            startTime = System.currentTimeMillis()
        }
    }

    fun makeGuess(guess: String) {
        if (_gameState.value !is GameState.Playing) return

        viewModelScope.launch {
            try {
                val result = game.makeGuess(guess)
                val currentAttempts = _attempts.value - 1
                _attempts.emit(currentAttempts)

                // Tahmin geçmişini güncelle
                val guessResult = GuessResult(
                    guess = guess,
                    correct = result.correct,
                    misplaced = result.misplaced
                )
                _guessHistory.emit(_guessHistory.value + guessResult)

                // Skoru güncelle
                val timePenalty = ((System.currentTimeMillis() - startTime) / 1000) * 10
                val newScore = (_score.value - timePenalty).coerceAtLeast(0).toInt()
                _score.emit(newScore)

                when {
                    result.correct == 3 -> handleWin(newScore)
                    currentAttempts <= 0 -> handleLoss()
                }
            } catch (e: Exception) {
                errorHandler.handleError(
                    ErrorHandler.AppError.GameError("Invalid guess", e),
                    null
                )
            }
        }
    }

    private suspend fun handleWin(finalScore: Int) {
        _gameState.emit(GameState.Won(finalScore))
        saveGameResult(true, finalScore)
    }

    private suspend fun handleLoss() {
        _gameState.emit(GameState.Lost(game.getSecretNumber()))
        saveGameResult(false, 0)
    }

    private suspend fun saveGameResult(isWin: Boolean, score: Int) {
        val gameResult = GameResult(
            id = UUID.randomUUID().toString(),
            playerId = "current_player", // TODO: Gerçek oyuncu ID'si eklenecek
            score = score,
            attempts = 3 - _attempts.value,
            isWin = isWin,
            timeTaken = (System.currentTimeMillis() - startTime) / 1000,
            date = System.currentTimeMillis()
        )

        launchIO {
            gameRepository.saveGameResult(gameResult)
        }
    }

    sealed class GameState {
        object Initial : GameState()
        object Playing : GameState()
        data class Won(val score: Int) : GameState()
        data class Lost(val secretNumber: String) : GameState()
    }

    data class GuessResult(
        val guess: String,
        val correct: Int,
        val misplaced: Int
    )
}
