package ua.turskyi.travelling.extensions

import android.app.Activity
import android.graphics.Insets
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.view.WindowMetrics
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import ua.turskyi.travelling.common.view.InfoDialog

fun AppCompatActivity.openInfoDialog(info: String) {
    val infoDialog = InfoDialog.newInstance(info, false)
    infoDialog.show(this.supportFragmentManager, "info dialog")
}

fun AppCompatActivity.openInfoDialog(@StringRes info: Int, action: Boolean = false) {
    val infoDialog = InfoDialog.newInstance(getString(info), action)
    infoDialog.show(this.supportFragmentManager, "info dialog")
}

fun Activity.getScreenWidth() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    val windowMetrics: WindowMetrics = windowManager.currentWindowMetrics
    val insets: Insets = windowMetrics.windowInsets
        .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
    windowMetrics.bounds.width() - insets.left - insets.right
} else {
    val displayMetrics = DisplayMetrics()
    @Suppress("DEPRECATION")
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    displayMetrics.widthPixels
}

fun Activity.toastLong(@StringRes errorString: Int) =
    Toast.makeText(this, errorString, Toast.LENGTH_LONG).show()

fun Activity.toast(@StringRes message: Int) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
