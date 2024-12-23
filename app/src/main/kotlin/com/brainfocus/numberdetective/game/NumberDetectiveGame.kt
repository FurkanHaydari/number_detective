package com.brainfocus.numberdetective.game

import kotlin.random.Random

class NumberDetectiveGame {
    private var secretNumber: String = ""
    private var attempts: Int = 3
    private var gameStartTime: Long = 0L
    private var score: Int = 1000
    
    data class Guess(
        val correct: Int,  // Doğru yerdeki rakam sayısı
        val misplaced: Int // Yanlış yerdeki rakam sayısı
    )

    fun startNewGame() {
        secretNumber = generateSecretNumber()
        attempts = 3
        gameStartTime = System.currentTimeMillis()
        score = 1000
    }

    private fun generateSecretNumber(): String {
        val numbers = (0..9).shuffled().take(3)
        return numbers.joinToString("")
    }

    fun makeGuess(guessNumber: String): Guess {
        if (attempts <= 0) {
            throw IllegalStateException("No attempts left!")
        }
        
        attempts--
        
        var correct = 0
        var misplaced = 0
        
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
        
        // Skoru güncelle
        updateScore()
        
        return Guess(correct, misplaced)
    }

    private fun updateScore() {
        val timePassed = (System.currentTimeMillis() - gameStartTime) / 1000
        score -= (timePassed * 2).toInt() // Her saniye için 2 puan düş
        score = score.coerceAtLeast(0) // Skor 0'ın altına düşmesin
    }

    fun getRemainingAttempts(): Int = attempts
    
    fun getCurrentScore(): Int = score
    
    fun isGameWon(guess: String): Boolean = guess == secretNumber
    
    fun isGameOver(): Boolean = attempts <= 0
    
    fun getSecretNumber(): String = secretNumber
}
