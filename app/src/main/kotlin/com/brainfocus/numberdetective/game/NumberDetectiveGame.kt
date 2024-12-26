package com.brainfocus.numberdetective.game

import kotlin.random.Random

class NumberDetectiveGame {
    private var secretNumber = ""
    private var remainingAttempts = 3
    private var currentScore = 1000
    private var pathSelection = (1..3).random()
    private val numbers = (0..9).toMutableList()
    private var isGameWon = false
    
    // Hint patterns
    private val firstAChoices = listOf("xax", "xxa")
    private val firstBChoices = listOf("bxx", "xxb")
    private val firstCChoices = listOf("cxx", "xcx")
    
    private val onlyAAndCorrectChoices = listOf("axx")
    private val onlyBAndCorrectChoices = listOf("xbx")
    private val onlyCAndCorrectChoices = listOf("xxc")
    
    private val abFalse = listOf("bax", "bxa", "xab")
    private val acFalse = listOf("cax", "xca", "cxa")
    private val cbFalse = listOf("bcx", "cxb", "xcb")
    
    var firstHint = ""
    var secondHint = ""
    var thirdHint = ""
    var fourthHint = ""
    var fifthHint = ""
    
    fun getSecretNumber(): String = secretNumber
    
    fun getRemainingAttempts(): Int = remainingAttempts
    
    fun getCurrentScore(): Int = currentScore
    
    fun isGameWon(guess: String? = null): Boolean {
        if (guess != null) {
            return guess == secretNumber
        }
        return isGameWon
    }
    
    fun isGameOver(): Boolean = remainingAttempts <= 0 || isGameWon

    fun startNewGame() {
        numbers.clear()
        numbers.addAll((0..9))
        generateSecretNumber()
        remainingAttempts = 3
        currentScore = 1000
        isGameWon = false
        generateHints()
        makeReadableHints()
    }
    
    private fun generateSecretNumber() {
        val digits = numbers.shuffled().take(3)
        secretNumber = digits.joinToString("")
    }
    
    private fun generateHints() {
        val temp = Random.nextBoolean()
        
        when (pathSelection) {
            1 -> {
                firstHint = firstAChoices.random()
                secondHint = onlyAAndCorrectChoices.random()
                thirdHint = if (temp) abFalse.random() else acFalse.random()
                fourthHint = cbFalse.random()
                fifthHint = if (temp) "cbx" else "bxc"
            }
            2 -> {
                firstHint = firstBChoices.random()
                secondHint = onlyBAndCorrectChoices.random()
                thirdHint = if (temp) abFalse.random() else cbFalse.random()
                fourthHint = acFalse.random()
                fifthHint = if (temp) "acx" else "xac"
            }
            else -> {
                firstHint = firstCChoices.random()
                secondHint = onlyCAndCorrectChoices.random()
                thirdHint = if (temp) acFalse.random() else cbFalse.random()
                fourthHint = abFalse.random()
                fifthHint = if (temp) "axb" else "xba"
            }
        }
    }
    
    private fun makeReadableHints() {
        val numbers = (0..9).toMutableList()
        val digits = secretNumber.map { it.toString().toInt() }
        
        firstHint = makeReadable(firstHint, numbers, digits)
        secondHint = makeReadable(secondHint, numbers, digits)
        thirdHint = makeReadable(thirdHint, numbers, digits)
        fourthHint = makeReadable(fourthHint, numbers, digits)
        fifthHint = makeReadable(fifthHint, numbers, digits)
    }
    
    private fun makeReadable(hint: String, numbers: MutableList<Int>, digits: List<Int>): String {
        return hint.map { char ->
            when (char) {
                'x' -> numbers.random().also { numbers.remove(it) }.toString()
                'a' -> digits[0].toString()
                'b' -> digits[1].toString()
                'c' -> digits[2].toString()
                else -> ""
            }
        }.joinToString("")
    }
    
    fun makeGuess(guess: String): Guess {
        if (isGameOver()) {
            throw IllegalStateException("Game is over. Cannot make more guesses.")
        }

        var correct = 0
        var misplaced = 0

        // Check correct positions
        for (i in guess.indices) {
            if (guess[i] == secretNumber[i]) {
                correct++
            }
        }

        // Check misplaced numbers
        val guessDigits = guess.toSet()
        val secretDigits = secretNumber.toSet()
        misplaced = (guessDigits intersect secretDigits).size - correct

        remainingAttempts--
        currentScore -= 100

        if (correct == 3) {
            isGameWon = true
        }

        return Guess(correct, misplaced)
    }
    
    data class Guess(val correct: Int, val misplaced: Int)
}
