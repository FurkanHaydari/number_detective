package com.brainfocus.numberdetective

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.Logger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.android.gms.ads.MobileAds
import com.brainfocus.numberdetective.di.viewModelModule
import android.util.Log

class NumberDetectiveApp : Application() {
    companion object {
        private const val DATABASE_URL = "https://number-detective-686e2-default-rtdb.europe-west1.firebasedatabase.app"
    }

    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@NumberDetectiveApp)
            modules(listOf(
                viewModelModule
            ))
        }

        // Initialize Firebase components
        try {
            // Firebase Database initialization with correct region URL
            Firebase.database(DATABASE_URL).apply {
                setLogLevel(Logger.Level.DEBUG)
                setPersistenceEnabled(true)
                Log.d("Firebase", "Database initialized with Europe West 1 region")
            }
            
            // Initialize Firebase authentication
            val auth = FirebaseAuth.getInstance()
            auth.addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                if (user == null) {
                    auth.signInAnonymously()
                        .addOnSuccessListener {
                            Log.d("Firebase", "Anonymous auth initialized successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Anonymous auth initialization failed: ${e.message}")
                            // Try to save data locally if authentication fails
                            if (e.message?.contains("CONFIGURATION_NOT_FOUND") == true) {
                                Log.w("Firebase", "Firebase configuration not found. Please check google-services.json and SHA-256 fingerprint")
                            }
                        }
                }
            }
        } catch (e: Exception) {
            Log.e("Firebase", "Error initializing Firebase: ${e.message}")
        }
        
        // Initialize AdMob
        MobileAds.initialize(this)
    }
} 