package com.brainfocus.numberdetective.model

import android.net.Uri

data class PlayerScore(
    val id: String,
    val name: String,
    val score: Long,
    val rank: Long,
    val distance: Int = 0,
    val playerIcon: Uri? = null
)
