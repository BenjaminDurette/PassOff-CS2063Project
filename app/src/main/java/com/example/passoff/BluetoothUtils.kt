package com.example.passoff

import android.app.Activity
import android.content.pm.PackageManager
import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object BluetoothUtils {

    // Function to check if Bluetooth permissions are granted and request them if not
    fun areBluetoothPermissionsGranted(activity: Activity): Boolean {
        // Check if Bluetooth permissions are granted
        val bluetoothPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        val bluetoothAdminPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        val AccessFineLocationPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val AccessCoarseLocationPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        // Check additional permissions for Android 12 and above
        val bluetoothConnectPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val bluetoothScanPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        // If any of the permissions are not granted, request them
        if (!bluetoothPermission || !bluetoothAdminPermission || !bluetoothConnectPermission || !bluetoothScanPermission || !AccessFineLocationPermission || !AccessCoarseLocationPermission) {
            val permissionsToRequest = mutableListOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            }

            ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest.toTypedArray(),
                1 // Request code
            )
            return false
        }

        return true
    }
}