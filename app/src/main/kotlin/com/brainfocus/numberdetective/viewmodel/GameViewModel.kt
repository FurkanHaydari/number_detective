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

    private var targetNumber = 0
    private var attempts = 0
    private var score = 0
    private val maxAttempts = 10
    private var hints = mutableListOf<String>()

    private val numbers = mutableListOf<Int>()
    private var hundredDigit = 0
    private var tenDigit = 0
    private var oneDigit = 0

    // Hint pattern choices
    private val firstAChoices = listOf("xax", "xxa")
    private val firstBChoices = listOf("bxx", "xxb")
    private val firstCChoices = listOf("cxx", "xcx")

    private val onlyAAndItIsCorrectChoices = listOf("axx")
    private val onlyBAndItIsCorrectChoices = listOf("xbx")
    private val onlyCAndItIsCorrectChoices = listOf("xxc")

    private val abFalse = listOf("bax", "bxa", "xab")
    private val acFalse = listOf("cax", "xca", "cxa")
    private val cbFalse = listOf("bcx", "cxb", "xcb")

    private val aTrueButRemainCFalse = listOf("acx")
    private val aTrueButRemainBFalse = listOf("axb")

    private val bTrueButRemainAFalse = listOf("xba")
    private val bTrueButRemainCFalse = listOf("cbx")

    private val cTrueButRemainAFalse = listOf("xac")
    private val cTrueButRemainBFalse = listOf("bxc")

    private lateinit var firstChoices: List<String>
    private lateinit var secondChoices: List<String>
    private lateinit var thirdChoices: List<String>
    private lateinit var fourthChoices: List<String>
    private lateinit var fifthChoices: List<String>

    init {
        startNewGame()
    }

    fun startNewGame() {
        resetNumbers()
        generateNumberWith3Digits()
        attempts = 0
        score = 1000
        generateHintChoices()
        generateHints()
        _gameState.value = GameState.Playing(
            hints = hints,
            attempts = attempts,
            maxAttempts = maxAttempts,
            score = score
        )
    }

    private fun resetNumbers() {
        numbers.clear()
        numbers.addAll(0..9)
    }

    private fun generateNumberWith3Digits() {
        hundredDigit = numbers.random().also { numbers.remove(it) }
        tenDigit = numbers.random().also { numbers.remove(it) }
        oneDigit = numbers.random().also { numbers.remove(it) }
        targetNumber = hundredDigit * 100 + tenDigit * 10 + oneDigit
    }

    private fun generateHintChoices() {
        val pathSelection = Random.nextInt(1, 4)
        val temp = Random.nextBoolean()

        when (pathSelection) {
            1 -> {
                firstChoices = firstAChoices
                secondChoices = onlyAAndItIsCorrectChoices
                thirdChoices = if (temp) abFalse else acFalse
                fourthChoices = cbFalse
                fifthChoices = if (temp) bTrueButRemainCFalse else cTrueButRemainBFalse
            }
            2 -> {
                firstChoices = firstBChoices
                secondChoices = onlyBAndItIsCorrectChoices
                thirdChoices = if (temp) abFalse else cbFalse
                fourthChoices = acFalse
                fifthChoices = if (temp) aTrueButRemainCFalse else cTrueButRemainAFalse
            }
            3 -> {
                firstChoices = firstCChoices
                secondChoices = onlyCAndItIsCorrectChoices
                thirdChoices = if (temp) acFalse else cbFalse
                fourthChoices = abFalse
                fifthChoices = if (temp) aTrueButRemainBFalse else bTrueButRemainAFalse
            }
        }
    }

    private fun generateHints() {
        hints.clear()
        val firstHint = makeReadable(firstChoices.random())
        val secondHint = makeReadable(secondChoices.random())
        val thirdHint = makeReadable(thirdChoices.random())
        val fourthHint = makeReadable(fourthChoices.random())
        val fifthHint = makeReadable(fifthChoices.random())

        hints.apply {
            add("Hint 1: $firstHint - One number is correct but wrongly placed")
            add("Hint 2: $secondHint - One number is correct and correctly placed")
            add("Hint 3: $thirdHint - Two numbers are correct but wrongly placed")
            add("Hint 4: $fourthHint - Two numbers are correct but wrongly placed")
            add("Hint 5: $fifthHint - Two numbers are correct but one of them is correctly placed")
        }
    }

    private fun makeReadable(hint: String): String {
        val remainingNumbers = numbers.toMutableList()
        val switch = mapOf(
            'x' to { remainingNumbers.random().also { remainingNumbers.remove(it) }.toString() },
            'a' to { hundredDigit.toString() },
            'b' to { tenDigit.toString() },
            'c' to { oneDigit.toString() }
        )
        return hint.map { switch[it]?.invoke() ?: "" }.joinToString("")
    }

    fun makeGuess(guess: Int) {
        attempts++
        score = calculateScore(attempts)

        when {
            guess == targetNumber -> {
                handleWin()
            }
            attempts >= maxAttempts -> {
                handleLoss()
            }
            else -> {
                _gameState.value = GameState.Playing(
                    hints = hints,
                    attempts = attempts,
                    maxAttempts = maxAttempts,
                    score = score
                )
            }
        }
    }

    private fun calculateScore(attempts: Int): Int {
        return maxOf(1000 - (attempts - 1) * 100, 0)
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
}

sealed class GameState {
    object Initial : GameState()
    data class Playing(
        val hints: List<String>,
        val attempts: Int,
        val maxAttempts: Int,
        val score: Int
    ) : GameState()
    data class Won(val score: Int) : GameState()
    data class Lost(val targetNumber: Int) : GameState()
}
