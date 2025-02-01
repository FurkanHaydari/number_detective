package com.brainfocus.numberdetective.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val geocoder = Geocoder(context)

    suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    continuation.resume(location)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error getting location", e)
                    continuation.resume(null)
                }

            continuation.invokeOnCancellation {
                // Cancel any pending location requests if needed
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getCurrentLocation", e)
            continuation.resume(null)
        }
    }

    suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                var addressResult: String? = null
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    addressResult = addresses.firstOrNull()?.let { address ->
                        "${address.locality}, ${address.countryName}"
                    }
                }
                addressResult
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()?.let { address ->
                    "${address.locality}, ${address.countryName}"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting address", e)
            null
        }
    }

    companion object {
        private const val TAG = "LocationManager"
    }
}
