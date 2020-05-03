package ua.turskyi.travelling.extensions

import android.graphics.Bitmap
import android.graphics.Canvas
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