package com.brainfocus.numberdetective

import android.app.Application
import android.content.Context
import android.os.StrictMode
import android.util.Log
import androidx.multidex.MultiDex
import com.brainfocus.numberdetective.utils.PerformanceMonitor
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BrainFocusApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var performanceMonitor: PerformanceMonitor

    companion object {
        private const val TAG = "BrainFocusApplication"
    }

    override fun onCreate() {
        if (BuildConfig.DEBUG) {
            enableStrictMode()
        }
        
        super.onCreate()

        initializeApp()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    private fun initializeApp() {
        applicationScope.launch(Dispatchers.IO) {
            try {
                // Firebase başlatma
                FirebaseApp.initializeApp(this@BrainFocusApplication)

                // Performans monitörü başlatma
                performanceMonitor = PerformanceMonitor.getInstance(this@BrainFocusApplication)
                
                // Önbellekleri temizle
                clearOldCaches()
                
                // Diğer başlatma işlemleri
                initializeComponents()
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing app", e)
            }
        }
    }

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build()
        )

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build()
        )
    }

    private fun clearOldCaches() {
        try {
            // Geçici dosyaları temizle
            cacheDir.deleteRecursively()
            
            // Eski performans loglarını temizle
            val logFile = getFileStreamPath("performance_log.txt")
            if (logFile.exists() && logFile.length() > 5 * 1024 * 1024) { // 5MB
                logFile.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing caches", e)
        }
    }

    private fun initializeComponents() {
        // Gerekli bileşenleri başlat
        applicationScope.launch(Dispatchers.IO) {
            // Sound Manager
            SoundManager.getInstance(this@BrainFocusApplication)
            
            // Preferences Manager
            PreferencesManager.getInstance(this@BrainFocusApplication)
            
            // Mission Manager
            MissionManager.getInstance(this@BrainFocusApplication)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        // Düşük bellek durumunda önbellekleri temizle
        ImageUtils.clearCache()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            TRIM_MEMORY_RUNNING_CRITICAL,
            TRIM_MEMORY_RUNNING_LOW -> {
                // Kritik bellek durumunda önbellekleri temizle
                ImageUtils.clearCache()
            }
        }
    }
}
