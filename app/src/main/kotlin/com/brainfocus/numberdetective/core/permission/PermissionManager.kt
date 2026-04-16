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
        
        val BLUETOOTH_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf()
        }
    }

    fun hasBluetoothPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return BLUETOOTH_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        }
        return true
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

    fun requestAllPermissions(activity: Activity) {
        val neededPermissions = mutableListOf<String>()
        
        // Only check Bluetooth permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !hasBluetoothPermissions()) {
            neededPermissions.addAll(BLUETOOTH_PERMISSIONS)
        }
        
        if (neededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                neededPermissions.toTypedArray(),
                BLUETOOTH_PERMISSION_REQUEST_CODE
            )
        }
    }
}
