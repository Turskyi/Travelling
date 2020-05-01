package ua.turskyi.travelling.extensions

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.view.PixelCopy
import android.view.View

fun View.setDynamicVisibility(visibility: Boolean) {
    if (visibility) {
        this.animate().alpha(1.0f).duration = 2000
    } else {
        this.animate().alpha(0.0f).duration = 200
    }
}

fun View.mapViewToBitmap(): Bitmap? {
    val bitmap =
        Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    this.draw(canvas)
    return bitmap
}

fun View.mapViewToBitmap(defaultColor: Int): Bitmap? {
    val bitmap =
        Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(defaultColor)
    this.draw(canvas)
    return bitmap
}

fun View.mapViewToBitmap(activity: Activity, callback: (Bitmap) -> Unit) {
    activity.window?.let { window ->
        val bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
        val locationOfViewInWindow = IntArray(2)
        this.getLocationInWindow(locationOfViewInWindow)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PixelCopy.request(window, Rect(
                    locationOfViewInWindow[0], locationOfViewInWindow[1],
                    locationOfViewInWindow[0] + this.width, locationOfViewInWindow[1] +
                            this.height
                ), bitmap, { copyResult ->
                    if (copyResult == PixelCopy.SUCCESS) {
                        callback(bitmap)
                    }
                    // possible to handle other result codes ...
                }, Handler()
                )
            } else {
                this.drawingCache
            }
        } catch (e: IllegalArgumentException) {
            // PixelCopy may throw IllegalArgumentException, make sure to handle it
            e.printStackTrace()
        }
    }
}

