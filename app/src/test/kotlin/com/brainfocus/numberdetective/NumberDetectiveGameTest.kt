package com.brainfocus.numberdetective

import com.brainfocus.numberdetective.game.NumberDetectiveGame
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NumberDetectiveGameTest {
    private lateinit var game: NumberDetectiveGame

    @Before
    fun setup() {
        game = NumberDetectiveGame()
        game.startNewGame()
    }

    @Test
    fun testInitialState() {
        assertEquals(3, game.getRemainingAttempts())
        assertEquals(1000, game.getCurrentScore())
    }

    @Test
    fun testValidGuess() {
        val secretNumber = game.getSecretNumber()
        val result = game.makeGuess(secretNumber)
        
        assertEquals(3, result.correct)
        assertEquals(0, result.misplaced)
        assertTrue(game.isGameWon(secretNumber))
    }

    @Test
    fun testPartiallyCorrectGuess() {
        // Gizli sayıyı al ve ilk rakamı değiştirerek tahmin yap
        val secretNumber = game.getSecretNumber()
        val modifiedGuess = if (secretNumber[0] == '9') "0${secretNumber.substring(1)}" 
                           else "${secretNumber[0].toString().toInt() + 1}${secretNumber.substring(1)}"
        
        val result = game.makeGuess(modifiedGuess)
        assertEquals(2, result.correct) // Son iki rakam doğru
        assertEquals(0, result.misplaced) // İlk rakam yanlış ve başka bir yerde de yok
    }

    @Test
    fun testMisplacedDigits() {
        // Gizli sayının rakamlarının yerlerini değiştirerek tahmin yap
        val secretNumber = game.getSecretNumber()
        val reversedGuess = secretNumber.reversed()
        
        if (secretNumber != reversedGuess) {
            val result = game.makeGuess(reversedGuess)
            assertTrue(result.misplaced > 0)
        }
    }

    @Test
    fun testGameOver() {
        // 3 yanlış tahmin yap
        repeat(3) {
            game.makeGuess("123")
        }
        
        assertTrue(game.isGameOver())
        assertEquals(0, game.getRemainingAttempts())
    }

    @Test(expected = IllegalStateException::class)
    fun testExtraGuessAfterGameOver() {
        // 3 yanlış tahmin yap
        repeat(3) {
            game.makeGuess("123")
        }
        
        // 4. tahminde exception fırlatmalı
        game.makeGuess("123")
    }

    @Test
    fun testScoreDecrease() {
        val initialScore = game.getCurrentScore()
        Thread.sleep(1000) // 1 saniye bekle
        
        game.makeGuess("123")
        val newScore = game.getCurrentScore()
        
        assertTrue(newScore < initialScore)
    }
}
