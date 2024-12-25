package com.brainfocus.numberdetective.game

import kotlin.random.Random

class NumberDetectiveGame {
    private var secretNumber: String = ""
    private var remainingAttempts: Int = 3
    private var currentScore: Int = 1000
    private var isGameWon: Boolean = false
    
    data class Guess(
        val correct: Int,  // Doğru yerdeki rakam sayısı
        val misplaced: Int // Yanlış yerdeki rakam sayısı
    )

    fun startNewGame() {
        secretNumber = generateSecretNumber()
        remainingAttempts = 3
        currentScore = 1000
        isGameWon = false
    }

    private fun generateSecretNumber(): String {
        val numbers = (0..9).shuffled().take(3)
        return numbers.joinToString("")
    }

    fun makeGuess(guessNumber: String): Guess {
        if (isGameOver()) {
            throw IllegalStateException("Game is over. Cannot make more guesses.")
        }
        
        var correct = 0
        var misplaced : Int
        
        // Doğru yerdeki rakamları kontrol et
        for (i in guessNumber.indices) {
            if (guessNumber[i] == secretNumber[i]) {
                correct++
            }
        }
        
        // Yanlış yerdeki rakamları kontrol et
        val guessDigits = guessNumber.toSet()
        val secretDigits = secretNumber.toSet()
        misplaced = (guessDigits intersect secretDigits).size - correct
        
        // Update game state
        remainingAttempts--
        currentScore -= 100
        
        if (correct == 3) {
            isGameWon = true
        }
        
        return Guess(correct, misplaced)
    }

    fun isCorrectGuess(guess: Guess): Boolean {
        return guess.correct == 3
    }

    fun getSecretNumber(): String = secretNumber
    
    fun getRemainingAttempts(): Int = remainingAttempts
    
    fun getCurrentScore(): Int = currentScore
    
    fun isGameWon(): Boolean = isGameWon
    
    fun isGameOver(): Boolean = remainingAttempts <= 0 || isGameWon
    
    fun isGameWon(guess: String): Boolean {
        return guess == secretNumber
    }
}
