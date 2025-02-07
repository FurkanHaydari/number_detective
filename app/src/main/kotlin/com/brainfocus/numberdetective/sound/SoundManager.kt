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
    private var correctSoundId: Int = 0
    private var buttonClickId: Int = 0
    private var loseSoundId: Int = 0
    private var levelUpSoundId: Int = 0
    private var partialWrongSoundId: Int = 0
    private var isInitialized = false
    private var loadedSounds = 0
    private var totalSounds = 8
    
    fun initialize() {
        if (isInitialized) return
        
        try {
            android.util.Log.d("SoundManager", "Initializing SoundManager...")
            
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            
            soundPool = SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(attributes)
                .build()
            
            // Verify resources exist before loading
            val resourceIds = listOf(
                R.raw.tick_sound to "tick_sound",
                R.raw.win_sound to "win_sound",
                R.raw.wrong_guess to "wrong_guess",
                R.raw.correct_guess to "correct_guess",
                R.raw.button_click to "button_click",
                R.raw.lose_sound to "lose_sound",
                R.raw.level_up to "level_up",
                R.raw.partial_or_wrong_guess to "partial_wrong"
            )
            
            for ((id, name) in resourceIds) {
                try {
                    val resourceType = context.resources.getResourceTypeName(id)
                    val resourceName = context.resources.getResourceEntryName(id)
                    android.util.Log.d("SoundManager", "Found resource: $name ($resourceType/$resourceName) with ID: $id")
                } catch (e: Exception) {
                    android.util.Log.e("SoundManager", "Resource not found: $name (ID: $id)")
                }
            }
            
            soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
                synchronized(this) {
                    if (status == 0) {
                        loadedSounds++
                        android.util.Log.d("SoundManager", "Sound loaded successfully: $sampleId, Total: $loadedSounds/$totalSounds")
                        if (loadedSounds == totalSounds) {
                            isInitialized = true
                            android.util.Log.d("SoundManager", "All sounds loaded successfully")
                        }
                    } else {
                        android.util.Log.e("SoundManager", "Failed to load sound $sampleId with status $status")
                    }
                }
            }
            
            // Reset counters and IDs
            loadedSounds = 0
            isInitialized = false
            tickSoundId = 0
            winSoundId = 0
            wrongSoundId = 0
            levelUpSoundId = 0
            partialWrongSoundId = 0
            
            android.util.Log.d("SoundManager", "Starting to load sounds...")
            
            // Load all sounds
            tickSoundId = loadSound(R.raw.tick_sound)
            winSoundId = loadSound(R.raw.win_sound)
            wrongSoundId = loadSound(R.raw.wrong_guess)
            correctSoundId = loadSound(R.raw.correct_guess)
            buttonClickId = loadSound(R.raw.button_click)
            loseSoundId = loadSound(R.raw.lose_sound)
            levelUpSoundId = loadSound(R.raw.level_up)
            partialWrongSoundId = loadSound(R.raw.partial_or_wrong_guess)
            
            android.util.Log.d("SoundManager", "Sound loading initiated")
            
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error initializing SoundManager", e)
            release()
        }
    }
    
    fun playTickSound() {
        if (!isInitialized) {
            android.util.Log.w("SoundManager", "Attempted to play tick sound when not initialized")
            return
        }
        if (tickSoundId == 0) {
            android.util.Log.e("SoundManager", "Invalid tick sound ID")
            return
        }
        try {
            val streamId = soundPool?.play(tickSoundId, 0.5f, 0.5f, 1, 0, 1f)
            if (streamId == 0) {
                android.util.Log.e("SoundManager", "Failed to play tick sound")
            } else {
                android.util.Log.d("SoundManager", "Playing tick sound on stream: $streamId")
                trackStreamId(streamId)
            }
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error playing tick sound", e)
        }
    }
    
    fun playWinSound() {
        if (!isInitialized) {
            android.util.Log.w("SoundManager", "Attempted to play win sound when not initialized")
            return
        }
        if (winSoundId == 0) {
            android.util.Log.e("SoundManager", "Invalid win sound ID")
            return
        }
        try {
            val streamId = soundPool?.play(winSoundId, 1f, 1f, 1, 0, 1f)
            if (streamId == 0) {
                android.util.Log.e("SoundManager", "Failed to play win sound")
            } else {
                android.util.Log.d("SoundManager", "Playing win sound on stream: $streamId")
                trackStreamId(streamId)
            }
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error playing win sound", e)
        }
    }
    
    fun playWrongSound() {
        if (!isInitialized) {
            android.util.Log.w("SoundManager", "Attempted to play wrong sound when not initialized")
            return
        }
        if (wrongSoundId == 0) {
            android.util.Log.e("SoundManager", "Invalid wrong sound ID")
            return
        }
        try {
            val streamId = soundPool?.play(wrongSoundId, 1f, 1f, 1, 0, 1f)
            if (streamId == 0) {
                android.util.Log.e("SoundManager", "Failed to play wrong sound")
            } else {
                android.util.Log.d("SoundManager", "Playing wrong sound on stream: $streamId")
                trackStreamId(streamId)
            }
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error playing wrong sound", e)
        }
    }
    
    fun playCorrectSound() {
        if (!isInitialized) {
            android.util.Log.w("SoundManager", "Attempted to play correct sound when not initialized")
            return
        }
        if (correctSoundId == 0) {
            android.util.Log.e("SoundManager", "Invalid correct sound ID")
            return
        }
        try {
            val streamId = soundPool?.play(correctSoundId, 0.7f, 0.7f, 1, 0, 1f)
            if (streamId == 0) {
                android.util.Log.e("SoundManager", "Failed to play correct sound")
            } else {
                android.util.Log.d("SoundManager", "Playing correct sound on stream: $streamId")
                trackStreamId(streamId)
            }
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error playing correct sound", e)
        }
    }

    fun playButtonClick() {
        if (!isInitialized) {
            android.util.Log.w("SoundManager", "Attempted to play button click sound when not initialized")
            return
        }
        if (buttonClickId == 0) {
            android.util.Log.e("SoundManager", "Invalid button click sound ID")
            return
        }
        try {
            val streamId = soundPool?.play(buttonClickId, 0.5f, 0.5f, 1, 0, 1f)
            if (streamId == 0) {
                android.util.Log.e("SoundManager", "Failed to play button click sound")
            } else {
                android.util.Log.d("SoundManager", "Playing button click sound on stream: $streamId")
                trackStreamId(streamId)
            }
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error playing button click sound", e)
        }
    }

    fun playLoseSound() {
        if (!isInitialized) {
            android.util.Log.w("SoundManager", "Attempted to play lose sound when not initialized")
            return
        }
        if (loseSoundId == 0) {
            android.util.Log.e("SoundManager", "Invalid lose sound ID")
            return
        }
        try {
            val streamId = soundPool?.play(loseSoundId, 1f, 1f, 1, 0, 1f)
            if (streamId == 0) {
                android.util.Log.e("SoundManager", "Failed to play lose sound")
            } else {
                android.util.Log.d("SoundManager", "Playing lose sound on stream: $streamId")
                trackStreamId(streamId)
            }
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error playing lose sound", e)
        }
    }

    fun playLevelUpSound() {
        if (!isInitialized) {
            android.util.Log.w("SoundManager", "Attempted to play level up sound when not initialized")
            return
        }
        if (levelUpSoundId == 0) {
            android.util.Log.e("SoundManager", "Invalid level up sound ID")
            return
        }
        try {
            val streamId = soundPool?.play(levelUpSoundId, 1f, 1f, 1, 0, 1f)
            if (streamId == 0) {
                android.util.Log.e("SoundManager", "Failed to play level up sound")
            } else {
                android.util.Log.d("SoundManager", "Playing level up sound on stream: $streamId")
                trackStreamId(streamId)
            }
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error playing level up sound", e)
        }
    }

    fun playPartialWrongSound() {
        if (!isInitialized) {
            android.util.Log.w("SoundManager", "Attempted to play partial/wrong sound when not initialized")
            return
        }
        if (partialWrongSoundId == 0) {
            android.util.Log.e("SoundManager", "Invalid partial/wrong sound ID")
            return
        }
        try {
            val streamId = soundPool?.play(partialWrongSoundId, 1f, 1f, 1, 0, 1f)
            if (streamId == 0) {
                android.util.Log.e("SoundManager", "Failed to play partial/wrong sound")
            } else {
                android.util.Log.d("SoundManager", "Playing partial/wrong sound on stream: $streamId")
                trackStreamId(streamId)
            }
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error playing partial/wrong sound", e)
        }
    }

    private fun loadSound(resId: Int): Int {
        return try {
            // Validate resource exists
            val resourceName = context.resources.getResourceEntryName(resId)
            val resourceType = context.resources.getResourceTypeName(resId)
            
            android.util.Log.d("SoundManager", "Attempting to load sound: name=$resourceName, type=$resourceType, id=$resId")
            
            // Load using resource ID
            val soundId = soundPool?.load(context, resId, 1) ?: 0
            if (soundId == 0) {
                android.util.Log.e("SoundManager", "Failed to load sound: $resourceName (ID: $resId)")
            } else {
                android.util.Log.d("SoundManager", "Successfully loaded sound: $resourceName (ID: $resId) => $soundId")
            }
            soundId
        } catch (e: android.content.res.Resources.NotFoundException) {
            android.util.Log.e("SoundManager", "Resource not found: $resId")
            0
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error loading sound $resId: ${e.message}")
            0
        }
    }
    
    private var activeStreamIds = mutableSetOf<Int>()

    private fun trackStreamId(streamId: Int?) {
        if (streamId != null && streamId != 0) {
            synchronized(activeStreamIds) {
                activeStreamIds.add(streamId)
            }
            // Stream tamamlandığında ID'yi kaldır
            soundPool?.setOnLoadCompleteListener { _, sid, _ ->
                if (sid == streamId) {
                    synchronized(activeStreamIds) {
                        activeStreamIds.remove(streamId)
                    }
                }
            }
        }
    }

    fun release() {
        try {
            // Aktif stream'lerin bitmesini bekle
            var waitCount = 0
            while (activeStreamIds.isNotEmpty() && waitCount < 50) { // max 5 saniye bekle
                Thread.sleep(100)
                waitCount++
            }

            // Tüm aktif stream'leri durdur
            synchronized(activeStreamIds) {
                activeStreamIds.forEach { streamId ->
                    soundPool?.stop(streamId)
                }
                activeStreamIds.clear()
            }

            soundPool?.release()
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error releasing SoundPool", e)
        } finally {
            soundPool = null
            isInitialized = false
            loadedSounds = 0
            tickSoundId = 0
            winSoundId = 0
            wrongSoundId = 0
            correctSoundId = 0
            buttonClickId = 0
            loseSoundId = 0
            levelUpSoundId = 0
            partialWrongSoundId = 0
        }
    }
}
