package com.brainfocus.numberdetective

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.brainfocus.numberdetective.feature.AppNavigation
import com.brainfocus.numberdetective.core.designsystem.NumberDetectiveTheme
import com.brainfocus.numberdetective.core.utils.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private var soundPool: SoundPool? = null
    private var buttonClickSound = 0

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SoundPool
        lifecycleScope.launch {
            soundPool = SoundPool.Builder().setMaxStreams(5)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                ).build()
            buttonClickSound = withContext(Dispatchers.IO) {
                soundPool?.load(applicationContext, R.raw.button_click, 1) ?: 0
            }
        }

        setContent {
            val currentLanguage = remember { mutableStateOf(LocaleHelper.getLanguage(this)) }
            
            NumberDetectiveTheme {
                AppNavigation(
                    onPlayClick = {
                        playButtonClickSound()
                    },
                    onLanguageChange = { lang ->
                        LocaleHelper.setLocale(this@MainActivity, lang)
                        currentLanguage.value = lang
                        recreate() // Recreate activity to apply new language across the app
                    },
                    currentLanguage = currentLanguage.value
                )
            }
        }
    }

    private fun playButtonClickSound() {
        soundPool?.play(buttonClickSound, 1f, 1f, 1, 0, 1f)
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool?.release()
        soundPool = null
    }
}
