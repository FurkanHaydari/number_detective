package com.brainfocus.numberdetective.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brainfocus.numberdetective.utils.ErrorHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {
    
    protected val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    protected val _error = MutableSharedFlow<ErrorHandler.AppError>()
    val error: SharedFlow<ErrorHandler.AppError> = _error

    protected fun launchIO(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        showLoading: Boolean = true,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch(dispatcher) {
            try {
                if (showLoading) _loading.emit(true)
                block()
            } catch (e: Exception) {
                _error.emit(ErrorHandler.AppError.UnknownError(e.message ?: "Unknown error", e))
            } finally {
                if (showLoading) _loading.emit(false)
            }
        }
    }

    protected fun launchMain(
        dispatcher: CoroutineDispatcher = Dispatchers.Main,
        showLoading: Boolean = true,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch(dispatcher) {
            try {
                if (showLoading) _loading.emit(true)
                block()
            } catch (e: Exception) {
                _error.emit(ErrorHandler.AppError.UnknownError(e.message ?: "Unknown error", e))
            } finally {
                if (showLoading) _loading.emit(false)
            }
        }
    }
}
