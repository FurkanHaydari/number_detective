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

    private val viewCache = mutableMapOf<Int, View>()
    private var recyclerView: RecyclerView? = null
    
    override fun onAttachedToWindow(view: RecyclerView) {
        super.onAttachedToWindow(view)
        recyclerView = view
        
        // RecyclerView optimizasyonları
        view.apply {
            setItemViewCacheSize(20)
            // Deprecated olan kodları kaldır:

            // isDrawingCacheEnabled = true
            // drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH

            // Modern hardware acceleration kullan:
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }
    }

    override fun onDetachedFromWindow(view: RecyclerView?, recycler: RecyclerView.Recycler?) {
        super.onDetachedFromWindow(view, recycler)
        clearViewCache()
        recyclerView = null
    }

    override fun addView(child: View) {
        // View'ı cache'e ekle
        // val position = getPosition(child)
        // viewCache[position] = child
        super.addView(child)
        child.setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }

    override fun removeView(child: View) {
        // View'ı cache'den kaldır
        val position = getPosition(child)
        viewCache.remove(position)
        super.removeView(child)
    }

    override fun getChildAt(index: Int): View? {
        return viewCache[index] ?: super.getChildAt(index)
    }

    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        // Scroll performansını artır
        recyclerView?.let {
            if (!it.isAnimating && !state?.isMeasuring!!) {
                it.suppressLayout(true)
                val scroll = super.scrollVerticallyBy(dy, recycler, state)
                it.suppressLayout(false)
                return scroll
            }
        }
        return super.scrollVerticallyBy(dy, recycler, state)
    }

    private fun clearViewCache() {
        viewCache.clear()
    }

    fun enablePrefetch() {
        isItemPrefetchEnabled = true
    }

    fun disablePrefetch() {
        isItemPrefetchEnabled = false
    }
}
