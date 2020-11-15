package ua.turskyi.travelling.extensions

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import ua.turskyi.travelling.R

fun View.setDynamicVisibility(visibility: Boolean) = if (visibility) {
        this.animate().alpha(1.0f).duration = 2000
    } else {
        this.animate().alpha(0.0f).duration = 200
    }

fun View.convertViewToBitmap(): Bitmap? {
    val bitmap =
        Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    this.draw(canvas)
    return bitmap
}

fun View.getScreenShot() = convertViewToBitmap()?.let { Bitmap.createBitmap(it) }

/**
 * Show a snackbar with [messageRes]
 */
fun View.showShortMsg(@StringRes messageRes: Int, duration: Int = Snackbar.LENGTH_SHORT) {
    val mSnackbar: Snackbar = Snackbar.make(this, messageRes, duration)
    mSnackbar.config(this.context)
    mSnackbar.show()
}

fun View.showSnackBar(@StringRes msgString: Int) = showShortMsg(msgString)

inline fun View.longSnackWithAction(
    message: String,
    length: Int = Snackbar.LENGTH_LONG,
    function: Snackbar.() -> Unit
) {
    val snack = Snackbar.make(this, message, length)
    snack.config(this.context)
    val snackView = snack.view
    val snackTextView =
        snackView.findViewById<TextView>(R.id.snackbar_text)
    snackTextView?.setTextColor(Color.RED)
    snack.function()
    snack.show()
}

inline fun View.showSnackWithAction(
    message: String,
    length: Int = Snackbar.LENGTH_LONG,
    function: Snackbar.() -> Unit
) = longSnackWithAction(message, length, function)
