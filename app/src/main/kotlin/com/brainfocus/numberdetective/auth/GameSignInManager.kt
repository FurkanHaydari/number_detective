package com.brainfocus.numberdetective.auth

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.games.Games

class GameSignInManager(private val activity: Activity) {
    
    companion object {
        private const val TAG = "GameSignInManager"
        const val RC_SIGN_IN = 9001
    }
    
    private lateinit var signInClient: GoogleSignInClient
    private var onSignInSuccess: ((GoogleSignInAccount) -> Unit)? = null
    private var onSignInFailed: (() -> Unit)? = null
    
    fun initializeSignIn() {
        val signInConfig = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .requestEmail()
            .build()
            
        signInClient = GoogleSignIn.getClient(activity, signInConfig)
    }
    
    fun signIn(onSuccess: (GoogleSignInAccount) -> Unit, onFailed: () -> Unit) {
        this.onSignInSuccess = onSuccess
        this.onSignInFailed = onFailed
        
        val account = GoogleSignIn.getLastSignedInAccount(activity)
        if (account != null && GoogleSignIn.hasPermissions(account)) {
            onSignInSuccess?.invoke(account)
        } else {
            val intent = signInClient.signInIntent
            activity.startActivityForResult(intent, RC_SIGN_IN)
        }
    }
    
    fun handleSignInResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.result
                Log.d(TAG, "Sign in success: ${account.displayName}")
                onSignInSuccess?.invoke(account)
            } catch (e: Exception) {
                Log.e(TAG, "Sign in failed: ${e.message}")
                onSignInFailed?.invoke()
            }
        }
    }
    
    fun signOut() {
        signInClient.signOut().addOnCompleteListener {
            Log.d(TAG, "User signed out")
        }
    }
    
    fun getLeaderboardClient() = GoogleSignIn.getLastSignedInAccount(activity)?.let { account ->
        Games.getLeaderboardsClient(activity, account)
    }
}
