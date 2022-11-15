package ua.turskyi.travelling.utils.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.TypedArray
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ImageSpan
import android.widget.Toast
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import ua.turskyi.travelling.R
import ua.turskyi.travelling.features.home.view.ui.HomeActivity

/**
 * Starts the Activity [A], in a more concise way, while still allowing to configure the [Intent] in
 * the optional [configIntent] lambda.
 */
inline fun <reified A : Activity> Context.start(configIntent: Intent.() -> Unit = {}) {
    startActivity(Intent(this, A::class.java).apply(configIntent))
}

fun Context.isFacebookInstalled() = try {
    @Suppress("DEPRECATION")
    packageManager.getPackageInfo(
        getString(R.string.facebook_package),
        PackageManager.GET_META_DATA,
    )
    true
} catch (e: PackageManager.NameNotFoundException) {
    false
}

fun Context.spToPix(@DimenRes sizeRes: Int): Float {
    return resources.getDimension(sizeRes) / resources.displayMetrics.density
}

fun Context.getHomeActivity(): HomeActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is HomeActivity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

fun Context.getAppCompatActivity(): AppCompatActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is AppCompatActivity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

fun Context.getFragmentActivity(): FragmentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is FragmentActivity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

fun Context.convertPictureToSpannableString(imgRes: Int): SpannableString {
    val imageSpan = ImageSpan(this, imgRes)
    val spannableString = SpannableString(" ")
    spannableString.setSpan(imageSpan, " ".length - 1, " ".length, 0)
    return spannableString
}

fun <T> Context.openActivityWithArgs(destination: Class<T>, extras: Bundle.() -> Unit = {}) {
    val intent = Intent(this, destination)
    intent.putExtras(Bundle().apply(extras))
    startActivity(intent)
}

fun Context.getToolbarHeight(): Int {
    val styledAttributes: TypedArray =
        theme.obtainStyledAttributes(intArrayOf(R.attr.actionBarSize))
    val toolbarHeight = styledAttributes.getDimension(0, 0f).toInt()
    styledAttributes.recycle()
    return toolbarHeight
}

fun Context.toast(@StringRes msgResId: Int) {
    Toast.makeText(this, msgResId, Toast.LENGTH_SHORT).show()
}

fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

fun Context.toastLong(@StringRes msgResId: Int) {
    Toast.makeText(this, msgResId, Toast.LENGTH_LONG).show()
}

fun Context.toastLong(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

fun Context.showReportDialog() {
    val lastDialog: AlertDialog = AlertDialog.Builder(this)
        .setTitle(getString(R.string.alert_error_title))
        .setMessage(getString(R.string.alert_error_message))
        .setCancelable(false)
        .setPositiveButton(
            getString(R.string.yes)
        ) { _: DialogInterface?, _: Int ->
            val intent = Intent(
                Intent.ACTION_SENDTO,
                Uri.fromParts(
                    getString(R.string.scheme_mailto),
                    getString(R.string.email),
                    /* Gets the decoded fragment part of this URI,
                     * everything after the '#', null if undefined. */
                    null
                )
            )
            intent.putExtra(
                Intent.EXTRA_SUBJECT,
                getString(R.string.error_message_intro) + this.toString(),
            )
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }.setNegativeButton(
            getString(R.string.no)
        ) { dialog: DialogInterface, _: Int ->
            toast(R.string.error_message_try_tomorrow)
            dialog.cancel()
            val newIntent = Intent(this, javaClass)
            startActivity(newIntent)
        }.create()
    lastDialog.show()
}