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
    
    fun getCorrectAnswer(): String = secretNumber
    
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
        pathSelection = (1..3).random()
        generateHints()
    }
    
    private fun generateSecretNumber() {
        val digits = numbers.shuffled().take(3)
        secretNumber = digits.joinToString("")
    }
    
    private fun generateHints() {
        val a = secretNumber[0]
        val b = secretNumber[1]
        val c = secretNumber[2]
        
        // x için kullanılabilecek rakamları hazırla (a, b ve c hariç)
        val availableX = (0..9).map { it.toString()[0] }
                              .filter { it != a && it != b && it != c }
                              .toMutableList()
        
        fun replaceX(pattern: String): String {
            val result = pattern.toCharArray()
            for (i in result.indices) {
                if (result[i] == 'x') {
                    val randomX = availableX.random()
                    availableX.remove(randomX)
                    result[i] = randomX
                } else if (result[i] == 'a') {
                    result[i] = a
                } else if (result[i] == 'b') {
                    result[i] = b
                } else if (result[i] == 'c') {
                    result[i] = c
                }
            }
            return String(result)
        }

        val temp = Random.nextBoolean()
        
        when (pathSelection) {
            1 -> {
                firstHint = replaceX(firstAChoices.random())
                secondHint = replaceX(onlyAAndCorrectChoices.random())
                thirdHint = replaceX(if (temp) abFalse.random() else acFalse.random())
                fourthHint = replaceX(cbFalse.random())
                fifthHint = replaceX(if (temp) "cbx" else "bxc")
            }
            2 -> {
                firstHint = replaceX(firstBChoices.random())
                secondHint = replaceX(onlyBAndCorrectChoices.random())
                thirdHint = replaceX(if (temp) abFalse.random() else cbFalse.random())
                fourthHint = replaceX(acFalse.random())
                fifthHint = replaceX(if (temp) "acx" else "xac")
            }
            else -> {
                firstHint = replaceX(firstCChoices.random())
                secondHint = replaceX(onlyCAndCorrectChoices.random())
                thirdHint = replaceX(if (temp) acFalse.random() else cbFalse.random())
                fourthHint = replaceX(abFalse.random())
                fifthHint = replaceX(if (temp) "axb" else "xba")
            }
        }
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
