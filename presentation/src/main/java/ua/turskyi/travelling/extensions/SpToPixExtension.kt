package ua.turskyi.travelling.extensions

import android.content.Context
import androidx.annotation.DimenRes

fun Context.spToPix(@DimenRes sizeRes: Int): Float {
    return resources.getDimension(sizeRes) / resources.displayMetrics.density
}
