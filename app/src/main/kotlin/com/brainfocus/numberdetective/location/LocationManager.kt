package com.brainfocus.numberdetective.location

import android.Manifest
import android.annotation.SuppressLint
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

class LocationManager {
    companion object {
        private const val TAG = "LocationManager"
        private const val MIN_TIME_MS = 1000L
        private const val MIN_DISTANCE_M = 10f
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): GameLocation? {
        if (!hasLocationPermission(context)) {
            Log.d(TAG, "Location permission not granted")
            return null
        }

        return try {
            val locationClient = LocationServices.getFusedLocationProviderClient(context)
            val location = locationClient.lastLocation.await()
            location?.let { getAddressFromLocation(context, it) } ?: GameLocation()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting location: ${e.message}")
            GameLocation()
        }
    }

    private fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private suspend fun getAddressFromLocation(context: Context, location: Location): GameLocation = suspendCancellableCoroutine { continuation ->
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                    val address = addresses.firstOrNull()
                    continuation.resume(createGameLocation(address))
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val address = addresses?.firstOrNull()
                continuation.resume(createGameLocation(address))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting address: ${e.message}")
            continuation.resume(GameLocation())
        }
    }

    private fun createGameLocation(address: Address?): GameLocation {
        return if (address != null) {
            GameLocation(
                district = address.subLocality ?: address.subAdminArea,
                city = address.locality ?: address.adminArea,
                country = address.countryName ?: "Türkiye"
            )
        } else {
            GameLocation()
        }
    }
}
