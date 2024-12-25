package com.brainfocus.numberdetective.base

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

abstract class BaseActivity : AppCompatActivity() {

    private var progressBar: ProgressBar? = null
    private var rootView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    protected abstract fun setupViews()

    protected fun setLoadingView(progressBarView: ProgressBar, rootViewForSnackbar: View) {
        this.progressBar = progressBarView
        this.rootView = rootViewForSnackbar
    }

    protected fun <T> observeState(
        stateFlow: kotlinx.coroutines.flow.StateFlow<T>,
        onState: (T) -> Unit
    ) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                stateFlow.collect { state ->
                    onState(state)
                }
            }
        }
    }

    protected fun showError(message: String, retry: (() -> Unit)? = null) {
        rootView?.let { view ->
            val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            retry?.let {
                snackbar.setAction("Retry") { retry.invoke() }
            }
            snackbar.show()
        }
    }

    protected fun showLoading(show: Boolean) {
        progressBar?.visibility = if (show) View.VISIBLE else View.GONE
    }
}
