package com.brainfocus.numberdetective.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageUtils {
    private const val CACHE_SIZE = 4 * 1024 * 1024 // 4MB
    private val memoryCache = object : LruCache<String, Bitmap>(CACHE_SIZE) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    suspend fun loadOptimizedBitmap(
        context: Context,
        resourceId: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap = withContext(Dispatchers.IO) {
        val cacheKey = "${resourceId}_${reqWidth}_${reqHeight}"
        
        // Cache'den kontrol et
        memoryCache.get(cacheKey)?.let { return@withContext it }

        // Bitmap boyutlarını hesapla
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeResource(context.resources, resourceId, options)

        options.apply {
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
            inJustDecodeBounds = false
            inPreferredConfig = Bitmap.Config.RGB_565 // Daha az bellek kullanımı
        }

        // Bitmap'i yükle ve cache'e ekle
        BitmapFactory.decodeResource(context.resources, resourceId, options)?.also {
            memoryCache.put(cacheKey, it)
        } ?: throw IllegalStateException("Bitmap yüklenemedi")
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && 
                   halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun clearCache() {
        memoryCache.evictAll()
    }
}
