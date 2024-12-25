package com.brainfocus.numberdetective.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PerformanceMonitor private constructor(private val context: Context) : LifecycleObserver {
    private val handler = Handler(Looper.getMainLooper())
    private val metrics = mutableMapOf<String, Long>()
    private var isMonitoring = false
    
    companion object {
        private const val TAG = "PerformanceMonitor"
        private const val MONITOR_INTERVAL = 1000L // 1 saniye
        private const val MEMORY_THRESHOLD = 80 // %80 bellek kullanımı
        private const val FPS_THRESHOLD = 30 // 30 FPS altı
        
        @Volatile
        private var instance: PerformanceMonitor? = null
        
        fun getInstance(context: Context): PerformanceMonitor {
            return instance ?: synchronized(this) {
                instance ?: PerformanceMonitor(context).also { instance = it }
            }
        }
    }

    private val monitorRunnable = object : Runnable {
        override fun run() {
            if (isMonitoring) {
                checkMemoryUsage()
                checkFrameRate()
                logMetrics()
                handler.postDelayed(this, MONITOR_INTERVAL)
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun startMonitoring() {
        isMonitoring = true
        handler.post(monitorRunnable)
        Log.d(TAG, "Performance monitoring started")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stopMonitoring() {
        isMonitoring = false
        handler.removeCallbacks(monitorRunnable)
        Log.d(TAG, "Performance monitoring stopped")
    }

    private fun checkMemoryUsage() {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val usedMemory = memoryInfo.totalMem - memoryInfo.availMem
        val memoryUsagePercentage = (usedMemory.toFloat() / memoryInfo.totalMem.toFloat() * 100).toInt()

        metrics["memory_usage"] = memoryUsagePercentage.toLong()

        if (memoryUsagePercentage > MEMORY_THRESHOLD) {
            Log.w(TAG, "High memory usage: $memoryUsagePercentage%")
            // Bellek optimizasyonu önerilerini tetikle
            suggestMemoryOptimizations()
        }
    }

    private fun checkFrameRate() {
        val frameStats = Debug.getBinderThreadState()
        metrics["frame_rate"] = frameStats.toLong()

        if (frameStats < FPS_THRESHOLD) {
            Log.w(TAG, "Low frame rate: $frameStats FPS")
            // Performans optimizasyonu önerilerini tetikle
            suggestPerformanceOptimizations()
        }
    }

    private fun logMetrics() {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logEntry = StringBuilder("Performance Metrics [$timestamp]:\n")
        
        metrics.forEach { (key, value) ->
            logEntry.append("$key: $value\n")
        }
        
        Log.d(TAG, logEntry.toString())
        
        // Metrikleri dosyaya kaydet
        saveMetricsToFile(logEntry.toString())
    }

    private fun saveMetricsToFile(metrics: String) {
        try {
            val logFile = File(context.filesDir, "performance_log.txt")
            logFile.appendText("$metrics\n")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving metrics to file", e)
        }
    }

    private fun suggestMemoryOptimizations() {
        // Bellek optimizasyonu önerileri
        ImageUtils.clearCache() // Görsel önbelleğini temizle
        System.gc() // Garbage collection'ı öner
    }

    private fun suggestPerformanceOptimizations() {
        // Performans optimizasyonu önerileri
        Log.i(TAG, "Performance optimization suggestions:")
        Log.i(TAG, "1. Disable animations temporarily")
        Log.i(TAG, "2. Reduce view hierarchy depth")
        Log.i(TAG, "3. Use hardware acceleration")
    }

    fun getMetrics(): Map<String, Long> = metrics.toMap()

    fun clearMetrics() {
        metrics.clear()
    }
}
