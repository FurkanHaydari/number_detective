package com.brainfocus.numberdetective.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val BLUETOOTH_PERMISSION_REQUEST_CODE = 1001
        const val LOCATION_PERMISSION_REQUEST_CODE = 1002
        
        val BLUETOOTH_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf()
        }
        
        val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    fun hasBluetoothPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return BLUETOOTH_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        }
        return true
    }

    fun hasLocationPermissions(): Boolean {
        return LOCATION_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestBluetoothPermissions(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !hasBluetoothPermissions()) {
            ActivityCompat.requestPermissions(
                activity,
                BLUETOOTH_PERMISSIONS,
                BLUETOOTH_PERMISSION_REQUEST_CODE
            )
        }
    }

    fun requestLocationPermissions(activity: Activity) {
        if (!hasLocationPermissions()) {
            ActivityCompat.requestPermissions(
                activity,
                LOCATION_PERMISSIONS,
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    fun shouldShowLocationPermissionRationale(activity: Activity): Boolean {
        return LOCATION_PERMISSIONS.any {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
        }
    }

    fun requestAllPermissions(activity: Activity) {
        val neededPermissions = mutableListOf<String>()
        
        // Bluetooth izinleri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !hasBluetoothPermissions()) {
            neededPermissions.addAll(BLUETOOTH_PERMISSIONS)
        }
        
        // Konum izinleri
        if (!hasLocationPermissions()) {
            neededPermissions.addAll(LOCATION_PERMISSIONS)
        }
        
        if (neededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                neededPermissions.toTypedArray(),
                LOCATION_PERMISSION_REQUEST_CODE // Ana permission code olarak location'ı kullanalım
            )
        }
    }
}
