package com.brainfocus.numberdetective.viewmodel

import androidx.lifecycle.ViewModel
import com.brainfocus.numberdetective.game.NumberDetectiveGame
import com.brainfocus.numberdetective.model.GameState
import com.brainfocus.numberdetective.model.GuessResult
import com.brainfocus.numberdetective.model.Hint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val game: NumberDetectiveGame
) : ViewModel() {
    private var _attempts = 0
    private val _wrongAttempts = MutableStateFlow(0)
    val wrongAttempts: StateFlow<Int> = _wrongAttempts
    
    private val _remainingAttempts = MutableStateFlow(MAX_ATTEMPTS)
    val remainingAttempts: StateFlow<Int> = _remainingAttempts
    
    var startTime = System.currentTimeMillis()
        private set
        
    private val _guesses = MutableStateFlow<List<String>>(emptyList())
    val guesses: StateFlow<List<String>> = _guesses
    
    private val _gameState = MutableStateFlow<GameState>(GameState.Initial)
    val gameState: StateFlow<GameState> = _gameState
    
    private val _hints = MutableStateFlow<List<Hint>>(emptyList())
    val hints: StateFlow<List<Hint>> = _hints
    
    private val _score = MutableStateFlow(1000)
    val score: StateFlow<Int> = _score
    
    private val _correctAnswer = MutableStateFlow("")
    val correctAnswer: StateFlow<String> = _correctAnswer

    val attempts: Int
        get() = _attempts

    init {
        startNewGame()
    }

    fun startNewGame() {
        _attempts = 0
        _wrongAttempts.value = 0
        _remainingAttempts.value = MAX_ATTEMPTS
        _score.value = 1000
        startTime = System.currentTimeMillis()
        _guesses.value = emptyList()
        game.startNewGame()
        _correctAnswer.value = game.getCorrectAnswer()
        _gameState.value = GameState.Playing
        
        // Generate all hints at once
        _hints.value = listOf(
            Hint(game.firstHint, 1, 0, "Bir rakam doğru ama yanlış yerde"),
            Hint(game.secondHint, 1, 0, "Bir rakam doğru ve doğru yerde"),
            Hint(game.thirdHint, 0, 2, "İki rakam doğru ama yanlış yerde"),
            Hint(game.fourthHint, 0, 2, "İki rakam doğru ama yanlış yerde"),
            Hint(game.fifthHint, 1, 1, "İki rakam doğru ve bir tanesi doğru yerde")
        )
        
        updateScore()
    }

    fun makeGuess(guess: String): GuessResult {
        _attempts++
        
        // Add the guess to the list
        val currentGuesses = _guesses.value.toMutableList()
        currentGuesses.add(guess)
        _guesses.value = currentGuesses

        // Decrease remaining attempts
        _remainingAttempts.value = _remainingAttempts.value - 1

        val result = game.makeGuess(guess)
        
        // Always increment wrong attempts unless it's a complete match
        if (result.correct != 3) {
            _wrongAttempts.value = _wrongAttempts.value + 1
        }
        
        val guessResult = when {
            result.correct == 3 -> {
                _gameState.value = GameState.Win(_score.value)
                GuessResult.Correct
            }
            _wrongAttempts.value >= MAX_ATTEMPTS -> {
                _gameState.value = GameState.GameOver(_score.value)
                GuessResult.Wrong
            }
            else -> GuessResult.Partial(result.correct, result.misplaced)
        }
        
        updateScore()
        return guessResult
    }
    
    private fun updateScore() {
        val timePenalty = ((System.currentTimeMillis() - startTime) / 1000 * TIME_PENALTY).toInt()
        val attemptsPenalty = _attempts * ATTEMPT_PENALTY
        _score.value = maxOf(0, 1000 - timePenalty - attemptsPenalty)
    }
    
    fun getCorrectAnswer(): String = _correctAnswer.value
    
    fun getTimeInSeconds(): Long = (System.currentTimeMillis() - startTime) / 1000
    
    companion object {
        private const val MAX_ATTEMPTS = 3
        private const val TIME_PENALTY = 0.5
        private const val ATTEMPT_PENALTY = 50
    }
}
