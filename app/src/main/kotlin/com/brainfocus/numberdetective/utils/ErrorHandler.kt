package com.brainfocus.numberdetective.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.crashlytics.FirebaseCrashlytics

class ErrorHandler(private val context: Context) {
    
    fun handleError(error: Throwable) {
        Log.e(TAG, "Error occurred", error)
        FirebaseCrashlytics.getInstance().recordException(error)
        
        val errorMessage = when (error) {
            is IllegalStateException -> "Invalid app state: ${error.message}"
            is IllegalArgumentException -> "Invalid input: ${error.message}"
            is SecurityException -> "Security error: ${error.message}"
            else -> "An unexpected error occurred"
        }
        
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
    }
    
    companion object {
        private const val TAG = "ErrorHandler"
    }
}
