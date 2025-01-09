package com.brainfocus.numberdetective.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.brainfocus.numberdetective.model.GameLocation
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.Locale
import kotlin.coroutines.resume

class LocationManager(private val context: Context) {
    companion object {
        private const val TAG = "LocationManager"
        private const val MIN_TIME_MS = 1000L
        private const val MIN_DISTANCE_M = 10f
    }

    suspend fun getCurrentLocation(): GameLocation? {
        if (!hasLocationPermission()) {
            Log.d(TAG, "Location permission not granted")
            return null
        }

        return try {
            val locationClient = LocationServices.getFusedLocationProviderClient(context)
            val location = locationClient.lastLocation.await()
            location?.let { getAddressFromLocation(it) } ?: GameLocation()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting location: ${e.message}")
            GameLocation()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private suspend fun getAddressFromLocation(location: Location): GameLocation = suspendCancellableCoroutine { continuation ->
        try {
            val geocoder = Geocoder(context, Locale("tr"))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        val address = addresses[0]
                        continuation.resume(
                            GameLocation(
                                district = address.subLocality,
                                city = address.adminArea,
                                country = "Türkiye"
                            )
                        )
                    } else {
                        continuation.resume(GameLocation())
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    continuation.resume(
                        GameLocation(
                            district = address.subLocality,
                            city = address.adminArea,
                            country = "Türkiye"
                        )
                    )
                } else {
                    continuation.resume(GameLocation())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting address: ${e.message}")
            continuation.resume(GameLocation())
        }
    }
}
