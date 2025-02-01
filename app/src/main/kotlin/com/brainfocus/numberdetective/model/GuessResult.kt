package com.brainfocus.numberdetective.model

sealed class GuessResult {
    object Correct : GuessResult()
    object Wrong : GuessResult()
    data class Partial(val correctDigits: Int, val wrongPositionDigits: Int) : GuessResult()
} 