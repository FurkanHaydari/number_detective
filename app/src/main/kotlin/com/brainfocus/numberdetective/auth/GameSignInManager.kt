package com.brainfocus.numberdetective.auth

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.games.Games
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class GameSignInManager @Inject constructor() {
    private lateinit var signInClient: GoogleSignInClient
    private lateinit var activity: Activity
    private var pendingCallback: ((Boolean) -> Unit)? = null
    private lateinit var auth: FirebaseAuth
    
    fun initialize(activity: Activity) {
        this.activity = activity
        this.auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .build()
        signInClient = GoogleSignIn.getClient(activity, gso)
    }

    companion object {
        private const val TAG = "GameSignInManager"
        private const val RC_SIGN_IN = 9001
    }

    fun signInSilently(callback: (Boolean) -> Unit) {
        val currentAccount = GoogleSignIn.getLastSignedInAccount(activity)
        if (currentAccount != null && GoogleSignIn.hasPermissions(currentAccount)) {
            onConnected(currentAccount)
            callback(true)
        } else {
            signInClient.silentSignIn().addOnCompleteListener { task ->
                try {
                    val account = task.getResult(ApiException::class.java)
                    onConnected(account)
                    callback(true)
                } catch (e: ApiException) {
                    Log.d(TAG, "signInSilently failed: ${e.message}")
                    callback(false)
                }
            }
        }
    }

    fun signIn() {
        pendingCallback = null
        val intent = signInClient.signInIntent
        activity.startActivityForResult(intent, RC_SIGN_IN)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, callback: (Boolean) -> Unit) {
        if (requestCode == RC_SIGN_IN) {
            handleSignInResult(data)
            callback(true)
        }
    }

    fun signOut(callback: () -> Unit) {
        signInClient.signOut().addOnCompleteListener {
            Log.d(TAG, "Sign out completed")
            callback()
        }
    }

    private fun onConnected(account: GoogleSignInAccount) {
        Games.getGamesClient(activity, account)
            .setViewForPopups(activity.findViewById(android.R.id.content))
    }

    private fun handleSignInResult(data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            
            // Got Google Account, now get Firebase credential
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            
            // Sign in with Firebase
            auth.signInWithCredential(credential)
                .addOnSuccessListener { authResult ->
                    // Successfully signed in
                    val user = authResult.user
                    if (user != null) {
                        onSignInSuccess(user)
                    } else {
                        onSignInError("Kullanıcı bilgisi alınamadı")
                    }
                }
                .addOnFailureListener { e ->
                    onSignInError("Giriş başarısız: ${e.message}")
                }
        } catch (e: ApiException) {
            onSignInError("Google hesabı ile giriş başarısız: ${e.message}")
        }
    }

    private fun onSignInSuccess(user: com.google.firebase.auth.FirebaseUser) {
        // Add implementation for onSignInSuccess
    }

    private fun onSignInError(message: String) {
        // Add implementation for onSignInError
    }
}
