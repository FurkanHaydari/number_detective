package com.brainfocus.numberdetective.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.game.NumberDetectiveGame
import com.brainfocus.numberdetective.model.GameState
import com.brainfocus.numberdetective.model.GuessResult
import com.brainfocus.numberdetective.model.Hint
import com.brainfocus.numberdetective.sound.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class GameViewModel @Inject constructor(
    application: Application,
    private val game: NumberDetectiveGame,
    private val soundManager: SoundManager
) : AndroidViewModel(application) {
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
    
    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score
    
    private val _correctAnswer = MutableStateFlow("")
    val correctAnswer: StateFlow<String> = _correctAnswer

    private val _currentLevel = MutableStateFlow(1)
    val currentLevel: StateFlow<Int> = _currentLevel

    private val _remainingTime = MutableStateFlow(180) // 3 minutes in seconds
    val remainingTime: StateFlow<Int> = _remainingTime

    private var timerJob: Job? = null

    val attempts: Int
        get() = _attempts

    init {
        startNewGame()
    }

    fun startNewGame(isFirstGame: Boolean = true) {
        if (isFirstGame) {
            _attempts = 0
            _wrongAttempts.value = 0
            _remainingAttempts.value = MAX_ATTEMPTS
            _score.value = 0  // Başlangıç skoru 0
            startTime = System.currentTimeMillis()
            _remainingTime.value = 180 // Reset timer to 3 minutes
            _currentLevel.value = 1
        }
        
        _guesses.value = emptyList()
        game.startNewGame(_currentLevel.value)  // Level'ı game'e geçiriyoruz
        _correctAnswer.value = game.getCorrectAnswer()
        _gameState.value = GameState.Playing
        
        // Print correct answer to logcat for testing
        android.util.Log.d("GameViewModel", "Correct answer for level ${_currentLevel.value}: ${_correctAnswer.value}")
        
        // Generate all hints and shuffle them
        val hintList = mutableListOf<Hint>()
        
        if (_currentLevel.value == 3) {
            // Level 3: Sıralı hintler
            hintList.addAll(listOf(
                Hint(game.firstHint, 1, 0, getHintDescription(3, 1)),
                Hint(game.secondHint, 0, 1, getHintDescription(3, 2)),
                Hint(game.thirdHint, 1, 1, getHintDescription(3, 3)),
                Hint(game.fourthHint, 1, 1, getHintDescription(3, 4)),
                Hint(game.fifthHint, 1, 1, getHintDescription(3, 5))
            ))
            _hints.value = hintList
        } else {
            // Level 1-2 için tüm hintler
            val hints = listOf(
                Hint(game.firstHint, 1, 0, getHintDescription(_currentLevel.value, 1)),
                Hint(game.secondHint, 1, 0, getHintDescription(_currentLevel.value, 2)),
                Hint(game.thirdHint, 0, 2, getHintDescription(_currentLevel.value, 3)),
                Hint(game.fourthHint, 0, 2, getHintDescription(_currentLevel.value, 4)),
                Hint(game.fifthHint, 1, 1, getHintDescription(_currentLevel.value, 5))
            )
            
            // Level 1: Sıralı, Level 2: Karışık
            _hints.value = if (_currentLevel.value == 2) hints.shuffled() else hints
        }
        
        if (isFirstGame) {
            startTimer()
        }
    }

    fun nextLevel() {
        if (_gameState.value !is GameState.Playing) {
            return
        }
        val currentLevel = _currentLevel.value
        if (currentLevel >= MAX_LEVELS) {
            _gameState.value = GameState.Win(_score.value)
            timerJob?.cancel()
            return
        }
        
        _currentLevel.value = currentLevel + 1
        
        // Level yükseldikçe ek can ve süre ver
        when (_currentLevel.value) {
            2 -> {
                // 2. seviyeye geçişte 1 can
                _remainingAttempts.value = _remainingAttempts.value + 2
                // ve 40 saniye
                _remainingTime.value = _remainingTime.value + 40
            }
            3 -> {
                // 3. seviyeye geçişte 2 can
                _remainingAttempts.value = _remainingAttempts.value + 3
                // ve 80 saniye
                _remainingTime.value += 80
            }
        }
        
        
        // Oyun bitmemişse level up Müziğini çal
        if (_gameState.value !is GameState.Win && _gameState.value !is GameState.GameOver) {
            soundManager.playLevelUpSound()
        }
        
        startNewGame(false)
    }

    fun makeGuess(guess: String): GuessResult {
        // Input validasyonu ekle
        if (guess.isBlank() || !guess.all { it.isDigit() }) {
            return GuessResult.Invalid
        }
        val requiredLength = if (_currentLevel.value == 3) 4 else 3
        if (guess.length != requiredLength) {
            return GuessResult.Invalid
        }
        _attempts++
        
        // Add the guess to the list
        val currentGuesses = _guesses.value.toMutableList()
        currentGuesses.add(guess)
        _guesses.value = currentGuesses

        // Decrease remaining attempts
        _remainingAttempts.value = _remainingAttempts.value - 1

        val result = game.makeGuess(guess)
        
        // Check if the guess is correct based on the current level
        val requiredDigits = if (_currentLevel.value == 3) 4 else 3
        
        // Always increment wrong attempts unless it's a complete match
        if (result.correct != requiredDigits) {
            _wrongAttempts.value = _wrongAttempts.value + 1
        }
        
        val guessResult = when {
            result.correct == requiredDigits -> {
                calculateLevelScore()
                if (_currentLevel.value >= MAX_LEVELS) {
                    _gameState.value = GameState.Win(_score.value)
                    timerJob?.cancel()
                }
                GuessResult.Correct
            }
            _remainingAttempts.value <= 0 -> {  // wrongAttempts yerine remainingAttempts kontrolü
                _gameState.value = GameState.GameOver(_score.value)
                timerJob?.cancel()
                GuessResult.Wrong
            }
            else -> {
                soundManager.playPartialWrongSound()
                GuessResult.Partial(result.correct, result.misplaced)
            }
        }
        
        return guessResult
    }

    private fun calculateLevelScore() {
        val maxScorePerLevel = 1000 // Her level için maksimum puan
        
        // Süre bazlı düşüş (180 saniye üzerinden)
        val timePenalty = (180 - _remainingTime.value) * 2 // Her saniye için 2 puan düşüş
        
        // Deneme sayısı bazlı düşüş
        val attemptsPenalty = _attempts * 50 // Her deneme için 50 puan düşüş
        
        // Level skoru hesaplama
        val levelScore = maxOf(0, maxScorePerLevel - timePenalty - attemptsPenalty)
        
        // Toplam skora ekleme
        _score.value = _score.value + levelScore
        
        android.util.Log.d("GameViewModel", """
            Level ${_currentLevel.value} Score Calculation:
            Base Score: $maxScorePerLevel
            Time Penalty: $timePenalty (${180 - _remainingTime.value} seconds used)
            Attempts Penalty: $attemptsPenalty (${_attempts} attempts used)
            Level Score: $levelScore
            Total Score: ${_score.value}
        """.trimIndent())
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_remainingTime.value > 0 && _gameState.value is GameState.Playing) {
                delay(1000)
                _remainingTime.value = _remainingTime.value - 1
                
                if (_remainingTime.value == 0) {
                    _gameState.value = GameState.GameOver(_score.value)
                }
            }
        }
    }

    fun getTimeInSeconds(): Int {
        return ((System.currentTimeMillis() - startTime) / 1000).toInt()
    }

    private fun getHintDescription(level: Int, hintNumber: Int): String {
        val resId = if (level == 3) {
            when (hintNumber) {
                1 -> R.string.hint_l3_1
                2 -> R.string.hint_l3_2
                3 -> R.string.hint_l3_3
                4 -> R.string.hint_l3_4
                5 -> R.string.hint_l3_5
                else -> null
            }
        } else {
            when (hintNumber) {
                1 -> R.string.hint_1
                2 -> R.string.hint_2
                3 -> R.string.hint_3
                4 -> R.string.hint_4
                5 -> R.string.hint_5
                else -> null
            }
        }
        return resId?.let { getApplication<Application>().getString(it) } ?: ""
    }

    companion object {
        const val MAX_ATTEMPTS = 3
        const val MAX_LEVELS = 3  // Maximum 3 level
    }
}
