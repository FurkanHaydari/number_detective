package com.brainfocus.numberdetective.utils

import android.content.Context
import android.media.MediaPlayer
import com.brainfocus.numberdetective.R

class SoundManager private constructor(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var isSoundEnabled = true

    companion object {
        @Volatile
        private var instance: SoundManager? = null

        fun getInstance(context: Context): SoundManager {
            return instance ?: synchronized(this) {
                instance ?: SoundManager(context).also { instance = it }
            }
        }
    }

    fun playSound(soundType: SoundType) {
        if (!isSoundEnabled) return

        val soundResId = when (soundType) {
            SoundType.WIN -> R.raw.win_sound
            SoundType.LOSE -> R.raw.lose_sound
            SoundType.BUTTON_CLICK -> R.raw.button_click
        }

        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, soundResId).apply {
                setOnCompletionListener { release() }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleSound() {
        isSoundEnabled = !isSoundEnabled
    }

    fun isSoundEnabled() = isSoundEnabled

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

enum class SoundType {
    WIN, LOSE, BUTTON_CLICK
}
