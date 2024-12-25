package com.brainfocus.numberdetective.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brainfocus.numberdetective.utils.ErrorHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel(
    private val errorHandler: ErrorHandler,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    protected fun launchWithErrorHandling(
        showLoading: Boolean = true,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch(dispatcher) {
            try {
                if (showLoading) _isLoading.value = true
                block()
            } catch (e: Exception) {
                errorHandler.handleError(e)
                _error.value = e.message ?: "An unexpected error occurred"
            } finally {
                if (showLoading) _isLoading.value = false
            }
        }
    }

    protected fun clearError() {
        _error.value = null
    }
}
