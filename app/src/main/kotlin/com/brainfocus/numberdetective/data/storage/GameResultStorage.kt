package com.brainfocus.numberdetective.data.storage

import com.brainfocus.numberdetective.data.model.Hint

object GameResultStorage {
    var lastGameHints: List<Hint> = emptyList()
    var lastGameDurationSeconds: Int = 0
}
