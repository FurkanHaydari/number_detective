package com.brainfocus.numberdetective.utils

import android.content.Context
import android.media.MediaPlayer
import com.brainfocus.numberdetective.R

class SoundManager private constructor(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val preferencesManager = PreferencesManager.getInstance(context)

    companion object {
        @Volatile
        private var instance: SoundManager? = null

        fun getInstance(context: Context): SoundManager {
            return instance ?: synchronized(this) {
                instance ?: SoundManager(context).also { instance = it }
            }
        }
    }

    fun playWinSound() {
        if (preferencesManager.isSoundEnabled()) {
            playSound(R.raw.victory)
        }
    }

    fun playLoseSound() {
        if (preferencesManager.isSoundEnabled()) {
            playSound(R.raw.game_over)
        }
    }

    fun playWrongSound() {
        if (preferencesManager.isSoundEnabled()) {
            playSound(R.raw.wrong_guess)
        }
    }

    fun playCorrectSound() {
        if (preferencesManager.isSoundEnabled()) {
            playSound(R.raw.correct_guess)
        }
    }

    fun playSound(resourceId: Int) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, resourceId)
            mediaPlayer?.start()
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
