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
    
    private var _score = 1000
    val score: Int get() = _score

    init {
        startNewGame()
    }

    fun startNewGame() {
        _attempts = 0
        _score = 1000
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
    }

    fun makeGuess(guess: Int) {
        if (_gameState.value is GameState.Won || _gameState.value is GameState.Lost) {
            return
        }

        try {
            _attempts++
            val result = game.makeGuess(guess.toString())
            _score = calculateScore(result)
            
            when {
                result.correct == 3 -> _gameState.value = GameState.Won(_score)
                _attempts >= 3 -> _gameState.value = GameState.Lost
                else -> _gameState.value = GameState.Playing(_score)
            }
        } catch (e: IllegalStateException) {
            _gameState.value = GameState.Error(e.message ?: "Bilinmeyen hata")
        }
    }

    private fun calculateScore(guess: NumberDetectiveGame.Guess): Int {
        val baseScore = 1000
        val timeDeduction = (getGameTime() * 5).toInt()  // Her saniye 5 puan
        val attemptDeduction = _attempts * 100  // Her deneme 100 puan
        val correctBonus = guess.correct * 50   // Her doğru rakam 50 bonus puan
        
        return maxOf(0, baseScore - timeDeduction - attemptDeduction + correctBonus)
    }

    private fun updateGameState() {
        _gameState.value = GameState.Playing(_score)
    }

    fun getAttempts(): Int = _attempts

    fun getGameTime(): Long = (System.currentTimeMillis() - startTime) / 1000
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
