package com.brainfocus.numberdetective.utils

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OptimizedLayoutManager(
    context: Context,
    orientation: Int = RecyclerView.VERTICAL,
    reverseLayout: Boolean = false
) : LinearLayoutManager(context, orientation, reverseLayout) {

    override fun onAttachedToWindow(view: RecyclerView) {
        super.onAttachedToWindow(view)
        view.setItemViewCacheSize(20)
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }
}
