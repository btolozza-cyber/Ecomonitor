
package com.example.monitoreo_ambiental.bluetooth

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object BluetoothPermissions {

    private const val REQUEST_CODE = 100

    fun tienePermiso(context: Context): Boolean {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun solicitarPermiso(activity: Activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !tienePermiso(activity)
        ) {
            activity.requestPermissions(
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                REQUEST_CODE
            )
        }
    }
}
