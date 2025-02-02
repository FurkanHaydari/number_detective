package com.brainfocus.numberdetective.game

import kotlin.random.Random

class NumberDetectiveGame {
    companion object {
        private const val MAX_ATTEMPTS = 3
        private const val INITIAL_SCORE = 1000
        private const val DIGITS_LEVEL_1_2 = 3
        private const val DIGITS_LEVEL_3 = 4
        
        // Level 1-2 Hint patterns
        private val LEVEL_1_2_FIRST_A_CHOICES = listOf("xax", "xxa")
        private val LEVEL_1_2_FIRST_B_CHOICES = listOf("bxx", "xxb")
        private val LEVEL_1_2_FIRST_C_CHOICES = listOf("cxx", "xcx")
        
        private val LEVEL_1_2_ONLY_A_CORRECT = listOf("axx")
        private val LEVEL_1_2_ONLY_B_CORRECT = listOf("xbx")
        private val LEVEL_1_2_ONLY_C_CORRECT = listOf("xxc")
        
        private val LEVEL_1_2_AB_FALSE = listOf("bax", "bxa", "xab")
        private val LEVEL_1_2_AC_FALSE = listOf("cax", "xca", "cxa")
        private val LEVEL_1_2_CB_FALSE = listOf("bcx", "cxb", "xcb")
    }

    private var secretNumber = ""
    private var remainingAttempts = MAX_ATTEMPTS
    private var currentScore = INITIAL_SCORE
    private var pathSelection = (1..3).random()
    private val numbers = (0..9).toMutableList()
    private var isGameWon = false
    private var currentLevel = 1
    private var dummyNumbers = mutableListOf<Int>() // Level 3 için dummy rakamlar
    
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

    fun startNewGame(level: Int = 1) {
        currentLevel = level
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
        if (currentLevel == 3) {
            // Rastgele bir rakamı çıkar
            val excludedNumber = numbers.random()
            numbers.remove(excludedNumber)
            
            // Kalan rakamlardan 4 tanesini seç
            val selectedNumbers = numbers.shuffled().take(4)
            secretNumber = selectedNumbers.joinToString("")
            
            // Dummy kümeyi hazırla (secretNumber'da kullanılmayan rakamlar)
            dummyNumbers = numbers.filter { it.toString()[0] !in secretNumber }.toMutableList()
        } else {
            secretNumber = numbers.shuffled().take(DIGITS_LEVEL_1_2).joinToString("")
        }
    }
    
    private fun generateHints() {
        if (currentLevel == 3) {
            generateLevel3Hints()
        } else {
            generateLevel1And2Hints()
        }
    }
    
    private fun generateLevel3Hints() {
        val (a, b, c, d) = secretNumber.map { it.toString().toInt() }
        
        // 1. hint: a ve c doğru ve doğru yerde, b ve d dummy'den
        firstHint = buildString {
            append(a)
            append(dummyNumbers.random())
            append(c)
            append(dummyNumbers.random())
        }
        
        // 2. hint: a ve d var, d doğru yerde a yanlış yerde
        secondHint = buildString {
            val positions = (0..3).filter { it != 0 && it != 3 }.random() // a için 1 veya 2
            val chars = CharArray(4) { 
                when(it) {
                    positions -> a.toString()[0]
                    3 -> d.toString()[0]
                    else -> dummyNumbers.random().toString()[0]
                }
            }
            append(chars.joinToString(""))
        }
        
        // 3. hint: b ve d var, sadece b doğru yerde
        thirdHint = buildString {
            val positions = (0..3).filter { it != 1 && it != 3 }.random() // d için 0 veya 2
            val chars = CharArray(4) {
                when(it) {
                    1 -> b.toString()[0]
                    positions -> d.toString()[0]
                    else -> dummyNumbers.random().toString()[0]
                }
            }
            append(chars.joinToString(""))
        }
        
        // 4. hint: sadece d doğru yerde
        fourthHint = buildString {
            append(dummyNumbers.random())
            append(dummyNumbers.random())
            append(dummyNumbers.random())
            append(d)
        }
        
        // 5. hint boş bırakılacak
        fifthHint = ""
    }
    
    private fun generateLevel1And2Hints() {
        val (a, b, c) = secretNumber.map { it }
        
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
                LEVEL_1_2_FIRST_A_CHOICES.random(),
                LEVEL_1_2_ONLY_A_CORRECT.random(),
                if (temp) LEVEL_1_2_AB_FALSE.random() else LEVEL_1_2_AC_FALSE.random(),
                LEVEL_1_2_CB_FALSE.random(),
                if (temp) "cbx" else "bxc"
            )
            2 -> listOf(
                LEVEL_1_2_FIRST_B_CHOICES.random(),
                LEVEL_1_2_ONLY_B_CORRECT.random(),
                if (temp) LEVEL_1_2_AB_FALSE.random() else LEVEL_1_2_CB_FALSE.random(),
                LEVEL_1_2_AC_FALSE.random(),
                if (temp) "acx" else "xac"
            )
            else -> listOf(
                LEVEL_1_2_FIRST_C_CHOICES.random(),
                LEVEL_1_2_ONLY_C_CORRECT.random(),
                if (temp) LEVEL_1_2_AC_FALSE.random() else LEVEL_1_2_CB_FALSE.random(),
                LEVEL_1_2_AB_FALSE.random(),
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
        val digits = if (currentLevel == 3) DIGITS_LEVEL_3 else DIGITS_LEVEL_1_2
        require(guess.length == digits) { "Tahmin $digits basamaklı olmalıdır" }

        remainingAttempts--
        
        val secretChars = secretNumber.toCharArray()
        val guessChars = guess.toCharArray()
        
        var correct = 0
        var misplaced = 0
        
        // Mark used positions to avoid double counting
        val usedSecret = BooleanArray(digits) { false }
        val usedGuess = BooleanArray(digits) { false }
        
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
        
        if (correct == digits) {
            isGameWon = true
        }
        
        return GuessResult(correct, misplaced)
    }
    
    data class GuessResult(val correct: Int, val misplaced: Int)
}
