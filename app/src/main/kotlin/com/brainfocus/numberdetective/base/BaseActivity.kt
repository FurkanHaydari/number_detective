package com.brainfocus.numberdetective.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.brainfocus.numberdetective.utils.ErrorHandler
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

abstract class BaseActivity : AppCompatActivity() {
    
    protected abstract val viewModel: BaseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Error ve loading state'lerini observe et
        observeCommonStates()
    }

    private fun observeCommonStates() {
        // Loading state
        collectFlow(viewModel.loading) { isLoading ->
            handleLoading(isLoading)
        }

        // Error state
        collectFlow(viewModel.error) { error ->
            handleError(error)
        }
    }

    protected fun <T> collectFlow(flow: Flow<T>, action: suspend (T) -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collect { action(it) }
            }
        }
    }

    protected open fun handleLoading(isLoading: Boolean) {
        // Progress bar veya loading indicator göster/gizle
        findViewById<View>(android.R.id.content)?.let { rootView ->
            rootView.findViewById<View>(com.google.android.material.R.id.progress_circular)?.apply {
                visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    protected open fun handleError(error: ErrorHandler.AppError) {
        // Hata mesajını Snackbar ile göster
        findViewById<View>(android.R.id.content)?.let { rootView ->
            Snackbar.make(
                rootView,
                error.message,
                Snackbar.LENGTH_LONG
            ).apply {
                setAction("Tamam") { dismiss() }
                show()
            }
        }
    }

    protected fun showMessage(message: String) {
        findViewById<View>(android.R.id.content)?.let { rootView ->
            Snackbar.make(
                rootView,
                message,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
}
