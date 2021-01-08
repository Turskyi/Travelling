package ua.turskyi.travelling.utils

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ua.turskyi.travelling.common.Constants
import ua.turskyi.travelling.extensions.getHomeActivity

object PermissionHandler {
    var isPermissionGranted = false
    fun checkPermission(activity: AppCompatActivity) {
        val locationPermission = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val externalStoragePermission =
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        if (locationPermission != PackageManager.PERMISSION_GRANTED
            && externalStoragePermission != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission(activity)
        } else {
            isPermissionGranted = true
            activity.getHomeActivity()?.initObservers()
        }
    }

    fun requestPermission(activity: AppCompatActivity) = ActivityCompat.requestPermissions(
        activity,
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).toTypedArray(),
        Constants.ACCESS_LOCATION_AND_EXTERNAL_STORAGE
    )
}