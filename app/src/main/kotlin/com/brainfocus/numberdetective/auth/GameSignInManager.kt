package com.brainfocus.numberdetective.auth

import android.app.Activity
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.games.PlayGames
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import dagger.hilt.android.scopes.ActivityScoped

@ActivityScoped
class GameSignInManager @Inject constructor() {
    companion object {
        private const val TAG = "GameSignInManager"
    }

    sealed class SignInResult {
        object Success : SignInResult()
        data class Error(val exception: Exception) : SignInResult()
        object Cancelled : SignInResult()
    }

    private var isSigningIn = false
    private val callbacks = mutableListOf<SignInCallback>()

    interface SignInCallback {
        fun onSuccess()
        fun onFailure(exception: Exception)
    }

    fun addSignInCallback(callback: SignInCallback) {
        callbacks.add(callback)
    }

    fun removeSignInCallback(callback: SignInCallback) {
        callbacks.remove(callback)
    }

    private fun notifySuccess() {
        callbacks.forEach { it.onSuccess() }
    }

    private fun notifyFailure(exception: Exception) {
        callbacks.forEach { it.onFailure(exception) }
    }

    private fun checkPlayServicesAvailability(activity: Activity): Boolean {
        val availability = GoogleApiAvailability.getInstance()
        val resultCode = availability.isGooglePlayServicesAvailable(activity)
        
        if (resultCode != ConnectionResult.SUCCESS) {
            if (availability.isUserResolvableError(resultCode)) {
                availability.getErrorDialog(activity, resultCode, 9000)?.show()
            }
            Log.e(TAG, "Google Play Services is not available: $resultCode")
            return false
        }
        return true
    }



    suspend fun signInSilently(activity: Activity): SignInResult = withContext(Dispatchers.IO) {
        if (!checkPlayServicesAvailability(activity)) {
            return@withContext SignInResult.Error(Exception("Google Play Services not available"))
        }
        
        try {
            val gamesSignInClient = PlayGames.getGamesSignInClient(activity)
            val result = gamesSignInClient.isAuthenticated.await()
            
            if (result.isAuthenticated) {
                // Log.d(TAG, "Successfully authenticated with Play Games silently")
                notifySuccess()
                return@withContext SignInResult.Success
            }
            
            // Log.d(TAG, "Not authenticated with Play Games")
            SignInResult.Cancelled
        } catch (e: Exception) {
            Log.e(TAG, "Silent sign-in failed", e)
            notifyFailure(e)
            SignInResult.Error(e)
        }
    }

    suspend fun signIn(activity: Activity): SignInResult = withContext(Dispatchers.IO) {
        if (!checkPlayServicesAvailability(activity)) {
            return@withContext SignInResult.Error(Exception("Google Play Services not available"))
        }
        
        try {
            val gamesSignInClient = PlayGames.getGamesSignInClient(activity)
            val result = gamesSignInClient.signIn().await()
            
            if (result.isAuthenticated) {
                // Log.d(TAG, "Successfully authenticated with Play Games")
                notifySuccess()
                return@withContext SignInResult.Success
            }
            
            // Log.d(TAG, "Authentication cancelled or failed")
            SignInResult.Cancelled
        } catch (e: Exception) {
            Log.e(TAG, "Interactive sign-in failed", e)
            notifyFailure(e)
            SignInResult.Error(e)
        }
    }

    // Geriye uyumluluk için eski callback-based method
    fun signIn(activity: Activity, callback: (Boolean) -> Unit) {
        if (isSigningIn) {
            callback(false)
            return
        }
        
        val tempCallback = object : SignInCallback {
            override fun onSuccess() {
                callback(true)
                removeSignInCallback(this)
            }

            override fun onFailure(exception: Exception) {
                callback(false)
                removeSignInCallback(this)
            }
        }
        
        addSignInCallback(tempCallback)
        
        try {
            isSigningIn = true
            val gamesSignInClient = PlayGames.getGamesSignInClient(activity)
            gamesSignInClient.signIn()
                .addOnSuccessListener { signInResult ->
                    if (signInResult.isAuthenticated) {
                        notifySuccess()
                    } else {
                        notifyFailure(Exception("Authentication failed"))
                    }
                    isSigningIn = false
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error starting sign in intent", e)
                    notifyFailure(e)
                    isSigningIn = false
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error initiating sign in", e)
            notifyFailure(e)
            isSigningIn = false
        }
    }

    fun isAuthenticated(activity: Activity, callback: (Boolean) -> Unit) {
        PlayGames.getGamesSignInClient(activity)
            .isAuthenticated()
            .addOnCompleteListener { task ->
                callback(task.isSuccessful && task.result.isAuthenticated)
            }
    }
}