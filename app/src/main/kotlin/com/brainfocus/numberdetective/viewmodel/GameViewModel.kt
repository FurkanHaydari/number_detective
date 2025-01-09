package com.brainfocus.numberdetective.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.brainfocus.numberdetective.game.NumberDetectiveGame

class GameViewModel : ViewModel() {
    private val game = NumberDetectiveGame()
    private var _attempts = 0
    private val startTime = System.currentTimeMillis()
    private val _gameState = MutableStateFlow<GameState>(GameState.Initial)
    val gameState: StateFlow<GameState> = _gameState
    
    private val _hints = MutableStateFlow<List<Hint>>(emptyList())
    val hints: StateFlow<List<Hint>> = _hints
    
    private val _score = MutableStateFlow(1000)
    val score: StateFlow<Int> = _score

    init {
        startNewGame()
    }

    fun startNewGame() {
        _attempts = 0
        _score.value = 1000
        game.startNewGame()
        
        val gameHints = listOf(
            Hint(game.firstHint.map { it.toString().toInt() }, 
                "Bir rakam doğru ama yanlış yerde"),
            Hint(game.secondHint.map { it.toString().toInt() }, 
                "Bir rakam doğru ve doğru yerde"),
            Hint(game.thirdHint.map { it.toString().toInt() }, 
                "İki rakam doğru ama ikisi de yanlış yerde"),
            Hint(game.fourthHint.map { it.toString().toInt() }, 
                "İki rakam doğru ama ikisi de yanlış yerde"),
            Hint(game.fifthHint.map { it.toString().toInt() }, 
                "İki rakam doğru, biri doğru yerde, biri yanlış yerde")
        )
        _hints.value = gameHints
        updateGameState()
        updateScore()
    }

    fun makeGuess(guess: Int) {
        if (_gameState.value is GameState.Won || _gameState.value is GameState.Lost) {
            return
        }

        try {
            _attempts++
            val result = game.makeGuess(guess.toString())
            
            when {
                result.correct == 3 -> _gameState.value = GameState.Won(_score.value)
                _attempts >= 3 -> _gameState.value = GameState.Lost
                else -> _gameState.value = GameState.Playing(_score.value)
            }
        } catch (e: IllegalStateException) {
            _gameState.value = GameState.Error(e.message ?: "Bilinmeyen hata")
        }
        updateScore()
    }

    private fun updateGameState() {
        _gameState.value = GameState.Playing(_score.value)
    }

    fun getAttempts(): Int = _attempts

    fun getGameTime(): Long = (System.currentTimeMillis() - startTime) / 1000

    fun getCorrectAnswer(): String = game.getCorrectAnswer()

    fun updateScore() {
        val currentTime = System.currentTimeMillis()
        val elapsedSeconds = (currentTime - startTime) / 1000
        val timeBasedScore = maxOf(1000 - (elapsedSeconds * 2).toInt(), 0)
        val hintsBasedScore = maxOf(500 - (_hints.value.size * 100), 0)
        
        _score.value = timeBasedScore + hintsBasedScore
    }
}

sealed class GameState {
    object Initial : GameState()
    data class Playing(val score: Int, val attempts: Int = 0) : GameState()
    data class Won(val score: Int) : GameState()
    object Lost : GameState()
    data class Error(val message: String) : GameState()
}

data class Hint(
    val numbers: List<Int>,
    val description: String
)
