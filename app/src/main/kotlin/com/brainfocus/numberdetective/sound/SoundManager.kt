package com.brainfocus.numberdetective.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import androidx.annotation.RawRes
import com.brainfocus.numberdetective.R

class SoundManager(private val context: Context) {
    companion object {
        private const val TAG = "SoundManager"
    }

    private var soundPool: SoundPool? = null
    private var soundMap = mutableMapOf<Int, Int>()

    init {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attributes)
            .build()
            
        // Sesleri yükle
        loadSound(R.raw.button_click)
        loadSound(R.raw.win_sound)
        loadSound(R.raw.lose_sound)
        loadSound(R.raw.wrong_guess)
        loadSound(R.raw.tick_sound)
    }

    fun loadSound(@RawRes resourceId: Int) {
        try {
            soundPool?.load(context, resourceId, 1)?.let { soundId ->
                soundMap[resourceId] = soundId
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading sound: ${e.message}")
        }
    }

    fun playSound(@RawRes resourceId: Int) {
        try {
            soundMap[resourceId]?.let { soundId ->
                soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound: ${e.message}")
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        soundMap.clear()
    }
}
