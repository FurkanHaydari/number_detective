package com.brainfocus.numberdetective.game

import kotlin.random.Random

class NumberDetectiveGame {
    companion object {
        private const val MAX_ATTEMPTS = 3
        private const val INITIAL_SCORE = 1000
        private const val DIGITS = 3
        
        // Hint patterns
        private val FIRST_A_CHOICES = listOf("xax", "xxa")
        private val FIRST_B_CHOICES = listOf("bxx", "xxb")
        private val FIRST_C_CHOICES = listOf("cxx", "xcx")
        
        private val ONLY_A_AND_CORRECT_CHOICES = listOf("axx")
        private val ONLY_B_AND_CORRECT_CHOICES = listOf("xbx")
        private val ONLY_C_AND_CORRECT_CHOICES = listOf("xxc")
        
        private val AB_FALSE = listOf("bax", "bxa", "xab")
        private val AC_FALSE = listOf("cax", "xca", "cxa")
        private val CB_FALSE = listOf("bcx", "cxb", "xcb")
    }

    private var secretNumber = ""
    private var remainingAttempts = MAX_ATTEMPTS
    private var currentScore = INITIAL_SCORE
    private var pathSelection = (1..3).random()
    private val numbers = (0..9).toMutableList()
    private var isGameWon = false
    
    var firstHint = ""
        private set
    var secondHint = ""
        private set
    var thirdHint = ""
        private set
    var fourthHint = ""
        private set
    var fifthHint = ""
        private set
    
    fun getSecretNumber(): String = secretNumber
    fun getRemainingAttempts(): Int = remainingAttempts
    fun getCurrentScore(): Int = currentScore
    fun getCorrectAnswer(): String = secretNumber
    fun isGameWon(guess: String? = null): Boolean = guess?.let { it == secretNumber } ?: isGameWon
    fun isGameOver(): Boolean = remainingAttempts <= 0 || isGameWon

    fun startNewGame() {
        numbers.clear()
        numbers.addAll(0..9)
        generateSecretNumber()
        remainingAttempts = MAX_ATTEMPTS
        currentScore = INITIAL_SCORE
        isGameWon = false
        pathSelection = (1..3).random()
        generateHints()
    }
    
    private fun generateSecretNumber() {
        secretNumber = numbers.shuffled().take(DIGITS).joinToString("")
    }
    
    private fun generateHints() {
        val (a, b, c) = secretNumber.map { it }
        
        // x için kullanılabilecek rakamları hazırla (a, b ve c hariç)
        val availableX = (0..9)
            .map { it.toString()[0] }
            .filterNot { it in setOf(a, b, c) }
            .toMutableList()
            .shuffled()
            .toMutableList()
        
        fun replaceX(pattern: String): String = buildString {
            pattern.forEach { char ->
                append(
                    when (char) {
                        'x' -> availableX.removeFirst()
                        'a' -> a
                        'b' -> b
                        'c' -> c
                        else -> char
                    }
                )
            }
        }

        val temp = Random.nextBoolean()
        
        val (first, second, third, fourth, fifth) = when (pathSelection) {
            1 -> listOf(
                FIRST_A_CHOICES.random(),
                ONLY_A_AND_CORRECT_CHOICES.random(),
                if (temp) AB_FALSE.random() else AC_FALSE.random(),
                CB_FALSE.random(),
                if (temp) "cbx" else "bxc"
            )
            2 -> listOf(
                FIRST_B_CHOICES.random(),
                ONLY_B_AND_CORRECT_CHOICES.random(),
                if (temp) AB_FALSE.random() else CB_FALSE.random(),
                AC_FALSE.random(),
                if (temp) "acx" else "xac"
            )
            else -> listOf(
                FIRST_C_CHOICES.random(),
                ONLY_C_AND_CORRECT_CHOICES.random(),
                if (temp) AC_FALSE.random() else CB_FALSE.random(),
                AB_FALSE.random(),
                if (temp) "axb" else "xba"
            )
        }

        firstHint = replaceX(first)
        secondHint = replaceX(second)
        thirdHint = replaceX(third)
        fourthHint = replaceX(fourth)
        fifthHint = replaceX(fifth)
    }
    
    fun makeGuess(guess: String): GuessResult {
        require(guess.length == DIGITS) { "Tahmin $DIGITS basamaklı olmalıdır" }

        remainingAttempts--
        
        val secretChars = secretNumber.toCharArray()
        val guessChars = guess.toCharArray()
        
        var correct = 0
        var misplaced = 0
        
        // Mark used positions to avoid double counting
        val usedSecret = BooleanArray(DIGITS) { false }
        val usedGuess = BooleanArray(DIGITS) { false }
        
        // First pass: count correct positions
        for (i in secretChars.indices) {
            if (secretChars[i] == guessChars[i]) {
                correct++
                usedSecret[i] = true
                usedGuess[i] = true
            }
        }
        
        // Second pass: count misplaced digits
        for (i in guessChars.indices) {
            if (!usedGuess[i]) {  // Skip if this position was already counted
                for (j in secretChars.indices) {
                    if (!usedSecret[j] && guessChars[i] == secretChars[j]) {
                        misplaced++
                        usedSecret[j] = true
                        usedGuess[i] = true
                        break
                    }
                }
            }
        }
        
        if (correct == DIGITS) {
            isGameWon = true
        }
        
        return GuessResult(correct, misplaced)
    }
    
    data class GuessResult(val correct: Int, val misplaced: Int)
}
