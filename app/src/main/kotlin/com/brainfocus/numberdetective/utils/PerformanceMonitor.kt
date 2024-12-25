package com.brainfocus.numberdetective.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.os.Process
import android.util.Log
import java.io.File
import java.io.RandomAccessFile

class PerformanceMonitor private constructor(private val context: Context) {
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val processId = Process.myPid()
    
    companion object {
        private const val TAG = "PerformanceMonitor"
        
        @Volatile
        private var instance: PerformanceMonitor? = null
        
        fun getInstance(context: Context): PerformanceMonitor {
            return instance ?: synchronized(this) {
                instance ?: PerformanceMonitor(context).also { instance = it }
            }
        }
    }

    fun getMemoryInfo(): String {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        
        val nativeHeapSize = Debug.getNativeHeapSize() / 1024
        val nativeHeapFreeSize = Debug.getNativeHeapFreeSize() / 1024
        val nativeHeapAllocatedSize = Debug.getNativeHeapAllocatedSize() / 1024
        
        return buildString {
            append("Memory Info:\n")
            append("Available Memory: ${memInfo.availMem / 1024 / 1024} MB\n")
            append("Total Memory: ${memInfo.totalMem / 1024 / 1024} MB\n")
            append("Low Memory: ${memInfo.lowMemory}\n")
            append("Native Heap Size: $nativeHeapSize KB\n")
            append("Native Heap Free: $nativeHeapFreeSize KB\n")
            append("Native Heap Allocated: $nativeHeapAllocatedSize KB")
        }
    }

    fun getCpuInfo(): String {
        return try {
            val cpuInfoFile = File("/proc/cpuinfo")
            if (!cpuInfoFile.exists()) return "CPU info not available"
            
            val cpuInfo = cpuInfoFile.readLines()
                .filter { it.startsWith("processor") || it.startsWith("model name") }
                .joinToString("\n")
            
            val cpuFreq = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
            val currentFreq = if (cpuFreq.exists()) {
                "Current CPU Frequency: ${cpuFreq.readText().trim().toLong() / 1000} MHz"
            } else {
                "CPU frequency info not available"
            }
            
            "$cpuInfo\n$currentFreq"
        } catch (e: Exception) {
            Log.e(TAG, "Error reading CPU info", e)
            "Error reading CPU info: ${e.message}"
        }
    }

    fun getProcessStats(): String {
        return try {
            val statFile = RandomAccessFile("/proc/$processId/stat", "r")
            val stats = statFile.readLine().split(" ")
            statFile.close()
            
            buildString {
                append("Process Stats:\n")
                append("PID: ${stats[0]}\n")
                append("Name: ${stats[1]}\n")
                append("State: ${stats[2]}\n")
                append("Parent PID: ${stats[3]}\n")
                append("Priority: ${stats[17]}\n")
                append("Threads: ${stats[19]}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading process stats", e)
            "Error reading process stats: ${e.message}"
        }
    }
}
