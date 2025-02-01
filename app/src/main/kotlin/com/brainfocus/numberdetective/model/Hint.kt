package com.brainfocus.numberdetective.model

data class Hint(
    val guess: String,
    val correct: Int,
    val misplaced: Int,
    val description: String = ""
)
