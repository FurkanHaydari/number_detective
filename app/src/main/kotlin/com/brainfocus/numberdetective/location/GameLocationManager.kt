package com.brainfocus.numberdetective.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class GameLocationManager(private val context: Context) {
    
    companion object {
        private const val TAG = "GameLocationManager"
    }
    
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }
    
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    suspend fun getCurrentDistrict(): String? = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission()) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location == null) {
                        Log.e(TAG, "Location is null")
                        continuation.resume(null)
                        return@addOnSuccessListener
                    }
                    
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            geocoder.getFromLocation(
                                location.latitude,
                                location.longitude,
                                1
                            ) { addresses ->
                                val district = addresses.firstOrNull()?.subAdminArea
                                continuation.resume(district)
                            }
                        } else {
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocation(
                                location.latitude,
                                location.longitude,
                                1
                            )
                            val district = addresses?.firstOrNull()?.subAdminArea
                            continuation.resume(district)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error getting district name: ${e.message}")
                        continuation.resume(null)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error getting location: ${e.message}")
                    continuation.resume(null)
                }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception: ${e.message}")
            continuation.resume(null)
        }
    }
}
