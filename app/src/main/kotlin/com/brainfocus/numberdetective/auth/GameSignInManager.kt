package com.brainfocus.numberdetective.auth

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.android.gms.games.Games
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class GameSignInManager(private val activity: Activity) {
    
    companion object {
        private const val TAG = "GameSignInManager"
        const val RC_SIGN_IN = 9001
    }
    
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private var onSignInSuccess: ((FirebaseUser) -> Unit)? = null
    private var onSignInFailed: (() -> Unit)? = null
    
    fun initializeSignIn() {
        oneTapClient = Identity.getSignInClient(activity)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(activity.getString(com.brainfocus.numberdetective.R.string.games_oauth_client_id))
                    .setFilterByAuthorizedAccounts(true)
                    .build())
            .build()
    }
    
    fun signIn(onSuccess: (FirebaseUser) -> Unit, onFailed: () -> Unit) {
        this.onSignInSuccess = onSuccess
        this.onSignInFailed = onFailed
        
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                try {
                    activity.startIntentSenderForResult(
                        result.pendingIntent.intentSender,
                        RC_SIGN_IN,
                        null,
                        0,
                        0,
                        0
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Couldn't start One Tap UI: ${e.message}")
                    onSignInFailed?.invoke()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Sign in failed: ${e.message}")
                onSignInFailed?.invoke()
            }
    }
    
    fun handleSignInResult(requestCode: Int, @Suppress("UNUSED_PARAMETER") resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
                        .addOnSuccessListener {
                            val user = FirebaseAuth.getInstance().currentUser
                            if (user != null) {
                                Log.d(TAG, "Sign in success: ${user.displayName}")
                                onSignInSuccess?.invoke(user)
                            } else {
                                Log.e(TAG, "User is null after successful sign in")
                                onSignInFailed?.invoke()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Firebase auth failed: ${e.message}")
                            onSignInFailed?.invoke()
                        }
                } else {
                    Log.e(TAG, "No ID token!")
                    onSignInFailed?.invoke()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sign in failed: ${e.message}")
                onSignInFailed?.invoke()
            }
        }
    }
    
    fun signOut() {
        FirebaseAuth.getInstance().signOut()
        Log.d(TAG, "User signed out")
    }
    
    fun getGamesClient() = GoogleSignIn.getLastSignedInAccount(activity)?.let { account ->
        Games.getGamesClient(activity, account)
    } ?: run {
        Log.e(TAG, "No signed in account found")
        null
    }
}
