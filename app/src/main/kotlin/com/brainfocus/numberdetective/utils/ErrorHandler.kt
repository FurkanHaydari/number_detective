package com.brainfocus.numberdetective.utils

import android.content.Context
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import android.app.Activity
import android.view.View
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class ErrorHandler private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "ErrorHandler"
        
        @Volatile
        private var instance: ErrorHandler? = null
        
        fun getInstance(context: Context): ErrorHandler {
            return instance ?: synchronized(this) {
                instance ?: ErrorHandler(context).also { instance = it }
            }
        }
    }

    sealed class AppError(val message: String, val throwable: Throwable? = null) {
        class NetworkError(message: String, throwable: Throwable? = null) : AppError(message, throwable)
        class DatabaseError(message: String, throwable: Throwable? = null) : AppError(message, throwable)
        class GameError(message: String, throwable: Throwable? = null) : AppError(message, throwable)
        class AuthError(message: String, throwable: Throwable? = null) : AppError(message, throwable)
        class UnknownError(message: String, throwable: Throwable? = null) : AppError(message, throwable)
    }

    fun handleError(error: AppError, activity: Activity?) {
        // Log hatayı
        Log.e(TAG, "Error: ${error.message}", error.throwable)
        
        // Crashlytics'e raporla
        FirebaseCrashlytics.getInstance().apply {
            recordException(error.throwable ?: Exception(error.message))
            setCustomKey("error_type", error::class.java.simpleName)
            setCustomKey("error_message", error.message)
        }

        // Kullanıcıya göster
        activity?.let { showErrorToUser(error, it) }
    }

    private fun showErrorToUser(error: AppError, activity: Activity) {
        val message = when (error) {
            is AppError.NetworkError -> "İnternet bağlantınızı kontrol edin"
            is AppError.DatabaseError -> "Veriler kaydedilirken bir hata oluştu"
            is AppError.GameError -> "Oyun sırasında bir hata oluştu"
            is AppError.AuthError -> "Oturum hatası"
            is AppError.UnknownError -> "Beklenmeyen bir hata oluştu"
        }

        Snackbar.make(
            activity.findViewById<View>(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        ).setAction("Tamam") {
            // Opsiyonel: Hata detaylarını göster
        }.show()
    }

    fun createCoroutineExceptionHandler(activity: Activity?): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, throwable ->
            handleError(
                AppError.UnknownError(
                    "Coroutine error: ${throwable.message}",
                    throwable
                ),
                activity
            )
        }
    }

    class ErrorHandlerContextElement(private val errorHandler: ErrorHandler, private val activity: Activity?) : 
        AbstractCoroutineContextElement(Key) {
        
        companion object Key : CoroutineContext.Key<ErrorHandlerContextElement>
        
        fun handleException(throwable: Throwable) {
            errorHandler.handleError(
                AppError.UnknownError(
                    "Coroutine error: ${throwable.message}",
                    throwable
                ),
                activity
            )
        }
    }
}
