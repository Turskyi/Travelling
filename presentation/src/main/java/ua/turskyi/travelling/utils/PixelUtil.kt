package ua.turskyi.travelling.utils

import android.content.Context

object PixelUtil {
    fun convertDipToPixels(context: Context, dips: Float): Int {
        return (dips * context.resources.displayMetrics.density + 0.5f).toInt()
    }
}