package com.brainfocus.numberdetective.auth

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.GamesClient
import com.google.android.gms.games.GamesSignInClient
import com.google.android.gms.games.Player
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import dagger.hilt.android.scopes.ActivityScoped

@ActivityScoped
class GameSignInManager @Inject constructor(
    private val activity: Activity
) {
    companion object {
        private const val TAG = "GameSignInManager"
        private const val RC_SIGN_IN = 9001
    }

    private var isSigningIn = false
    private var onSignInSuccessCallback: (() -> Unit)? = null
    private var onSignInFailedCallback: ((Exception) -> Unit)? = null

    suspend fun signInSilently(): Boolean {
        if (isSigningIn) return false
        
        return try {
            isSigningIn = true
            val gamesSignInClient = PlayGames.getGamesSignInClient(activity)
            val result = gamesSignInClient.isAuthenticated().await()
            
            if (result.isAuthenticated) {
                // Kullanıcı başarıyla giriş yaptı
                onSignInSuccessCallback?.invoke()
                true
            } else {
                // Kullanıcı giriş yapmadı, interaktif giriş başlat
                startSignInIntent()
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error signing in silently", e)
            onSignInFailedCallback?.invoke(e)
            false
        } finally {
            isSigningIn = false
        }
    }

    private fun startSignInIntent() {
        if (isSigningIn) return
        
        isSigningIn = true
        val gamesSignInClient = PlayGames.getGamesSignInClient(activity)
        gamesSignInClient.signIn()
            .addOnSuccessListener { signInResult ->
                if (signInResult.isAuthenticated) {
                    onSignInSuccessCallback?.invoke()
                } else {
                    onSignInFailedCallback?.invoke(Exception("Authentication failed"))
                }
                isSigningIn = false
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error starting sign in intent", e)
                onSignInFailedCallback?.invoke(e)
                isSigningIn = false
            }
    }

    fun setOnSignInSuccessListener(callback: () -> Unit) {
        onSignInSuccessCallback = callback
    }

    fun setOnSignInFailedListener(callback: (Exception) -> Unit) {
        onSignInFailedCallback = callback
    }

    fun signIn(activity: Activity, callback: (Boolean) -> Unit) {
        if (isSigningIn) {
            callback(false)
            return
        }
        
        isSigningIn = true
        val gamesSignInClient = PlayGames.getGamesSignInClient(activity)
        gamesSignInClient.signIn()
            .addOnSuccessListener { signInResult ->
                if (signInResult.isAuthenticated) {
                    callback(true)
                } else {
                    callback(false)
                }
                isSigningIn = false
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error starting sign in intent", e)
                callback(false)
                isSigningIn = false
            }
    }
}
