package com.brainfocus.numberdetective.sound

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

class SoundManager(private val context: Context) {
    companion object {
        private const val TAG = "SoundManager"
    }

    private var mediaPlayer: MediaPlayer? = null

    fun playSound(resourceId: Int) {
        try {
            releaseMediaPlayer()
            mediaPlayer = MediaPlayer.create(context, resourceId).apply {
                setOnCompletionListener { mp ->
                    mp.release()
                    mediaPlayer = null
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what extra=$extra")
                    mp.release()
                    mediaPlayer = null
                    true
                }
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound: ${e.message}")
            releaseMediaPlayer()
        }
    }

    fun releaseMediaPlayer() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
                mediaPlayer = null
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing MediaPlayer: ${e.message}")
            }
        }
    }
}
