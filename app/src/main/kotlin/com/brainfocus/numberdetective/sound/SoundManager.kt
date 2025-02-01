package com.brainfocus.numberdetective.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.brainfocus.numberdetective.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var soundPool: SoundPool? = null
    private var tickSoundId: Int = 0
    private var winSoundId: Int = 0
    private var wrongSoundId: Int = 0
    private var partialSoundId: Int = 0
    private var isInitialized = false
    
    fun initialize() {
        if (isInitialized) return
        
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attributes)
            .build()
        
        tickSoundId = soundPool?.load(context, R.raw.tick_sound, 1) ?: 0
        winSoundId = soundPool?.load(context, R.raw.win_sound, 1) ?: 0
        wrongSoundId = soundPool?.load(context, R.raw.wrong_guess, 1) ?: 0
        partialSoundId = soundPool?.load(context, R.raw.correct_guess, 1) ?: 0
        
        isInitialized = true
    }
    
    fun playTickSound() {
        soundPool?.play(tickSoundId, 0.5f, 0.5f, 0, 0, 1f)
    }
    
    fun playWinSound() {
        soundPool?.play(winSoundId, 1f, 1f, 0, 0, 1f)
    }
    
    fun playWrongSound() {
        soundPool?.play(wrongSoundId, 1f, 1f, 0, 0, 1f)
    }
    
    fun playPartialSound() {
        soundPool?.play(partialSoundId, 0.7f, 0.7f, 0, 0, 1f)
    }
    
    fun release() {
        soundPool?.release()
        soundPool = null
        isInitialized = false
    }
}
