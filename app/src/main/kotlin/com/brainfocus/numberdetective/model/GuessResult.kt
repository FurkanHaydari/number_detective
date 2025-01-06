package com.brainfocus.numberdetective.model

sealed class GuessResult {
    object Correct : GuessResult()
    data class Partial(val correctCount: Int, val misplacedCount: Int) : GuessResult()
    object Wrong : GuessResult()
} 