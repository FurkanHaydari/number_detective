package com.brainfocus.numberdetective.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache

object ImageUtils {
    private const val CACHE_SIZE = 4 * 1024 * 1024 // 4MB
    private val memoryCache = object : LruCache<String, Bitmap>(CACHE_SIZE) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    fun loadBitmapFromResource(context: Context, resourceId: Int): Bitmap? {
        val cacheKey = resourceId.toString()
        var bitmap = getBitmapFromCache(cacheKey)

        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
            if (bitmap != null) {
                addBitmapToCache(cacheKey, bitmap)
            }
        }

        return bitmap
    }

    private fun getBitmapFromCache(key: String): Bitmap? {
        return memoryCache.get(key)
    }

    private fun addBitmapToCache(key: String, bitmap: Bitmap) {
        if (getBitmapFromCache(key) == null) {
            memoryCache.put(key, bitmap)
        }
    }

    fun clearCache() {
        memoryCache.evictAll()
    }
}
