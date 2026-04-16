package com.brainfocus.numberdetective

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.brainfocus.numberdetective.data.ads.AdManager
import com.brainfocus.numberdetective.data.auth.GameSignInManager
import com.brainfocus.numberdetective.feature.AppNavigation
import com.brainfocus.numberdetective.core.designsystem.NumberDetectiveTheme
import com.brainfocus.numberdetective.core.utils.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var signInManager: GameSignInManager
    
    @Inject
    lateinit var adManager: AdManager

    private var soundPool: SoundPool? = null
    private var buttonClickSound = 0
    private var isSignedIn = mutableStateOf(false)

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SoundPool and AdManager
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
            adManager.initialize()
            
            // Try silent sign-in on startup
            val result = signInManager.signInSilently(this@MainActivity)
            handleSignInResult(result)
        }

        setContent {
            val currentLanguage = remember { mutableStateOf(LocaleHelper.getLanguage(this)) }
            
            NumberDetectiveTheme {
                AppNavigation(
                    onPlayClick = {
                        playButtonClickSound()
                        // Geçici olarak geliştirme ortamı için giriş devre dışı:
                        if (!isSignedIn.value) {
                            Toast.makeText(this@MainActivity, "Test: Google Play Devre Dışı", Toast.LENGTH_SHORT).show()
                            // startSignIn() // Play games popupını engelle
                        }
                    },
                    isSignedIn = { isSignedIn.value },
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

    private fun handleSignInResult(result: GameSignInManager.SignInResult) {
        when (result) {
            is GameSignInManager.SignInResult.Success -> {
                isSignedIn.value = true
            }
            is GameSignInManager.SignInResult.Cancelled,
            is GameSignInManager.SignInResult.Error -> {
                isSignedIn.value = false
                Log.e(TAG, "Sign in failed or cancelled")
            }
        }
    }

    private fun startSignIn() {
        lifecycleScope.launch {
            val result = signInManager.signIn(this@MainActivity)
            handleSignInResult(result)
        }
    }

    override fun onResume() {
        super.onResume()
        adManager.onResume()
    }

    override fun onPause() {
        super.onPause()
        adManager.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool?.release()
        soundPool = null
        adManager.release()
    }
}
