package com.brainfocus.numberdetective.viewmodel

import com.brainfocus.numberdetective.base.BaseViewModel
import com.brainfocus.numberdetective.utils.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

class GameViewModel(
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

    private var firstHint = ""
    private var secondHint = ""
    private var thirdHint = ""
    private var fourthHint = ""
    private var fifthHint = ""
    private var pathSelection = 1

    init {
        startNewGame()
    }

    fun startNewGame() {
        resetNumbers()
        generateNumberWith3Digits()
        attempts = 0
        score = 1000
        pathSelection = Random.nextInt(1, 4)
        generateHintChoices()
        generateHints()
        generateReadableHints()
        updateGameState()
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
        firstHint = firstChoices.random()
        secondHint = secondChoices.random()
        thirdHint = thirdChoices.random()
        fourthHint = fourthChoices.random()
        fifthHint = fifthChoices.random()
    }

    private fun generateReadableHints() {
        hints.clear()
        hints.add(makeReadableWithDescription(firstHint, "One number is correct but wrongly placed"))
        hints.add(makeReadableWithDescription(secondHint, "One number is correct and correctly placed"))
        hints.add(makeReadableWithDescription(thirdHint, "Two numbers are correct but wrongly placed"))
        hints.add(makeReadableWithDescription(fourthHint, "Two numbers are correct but wrongly placed"))
        hints.add(makeReadableWithDescription(fifthHint, "Two numbers are correct but one of them is correctly placed"))
    }

    private fun makeReadableWithDescription(hint: String, description: String): String {
        return "${makeReadable(hint)} - $description"
    }

    private fun makeReadable(hint: String): String {
        val switch = mapOf(
            'x' to { numbers.random().also { numbers.remove(it) }.toString() },
            'a' to { hundredDigit.toString() },
            'b' to { tenDigit.toString() },
            'c' to { oneDigit.toString() }
        )
        return hint.map { switch[it]?.invoke() ?: "" }.joinToString("")
    }

    fun makeGuess(guess: Int) {
        if (attempts < maxAttempts) {
            attempts++
            score -= 100

            if (guess == targetNumber) {
                _gameState.value = GameState.Won(score)
            } else if (attempts >= maxAttempts) {
                _gameState.value = GameState.Lost(targetNumber)
            } else {
                updateGameState(guess)
            }
        }
    }

    private fun updateGameState(lastGuess: Int? = null) {
        _gameState.value = GameState.Playing(
            hints = hints,
            attempts = attempts,
            maxAttempts = maxAttempts,
            score = score,
            lastGuess = lastGuess
        )
    }
}

sealed class GameState {
    object Initial : GameState()
    data class Playing(
        val hints: List<String>,
        val attempts: Int,
        val maxAttempts: Int,
        val score: Int,
        val lastGuess: Int? = null
    ) : GameState()
    data class Won(val score: Int) : GameState()
    data class Lost(val targetNumber: Int) : GameState()
}
