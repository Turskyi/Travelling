package ua.turskyi.travelling.utils.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.DimenRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import ua.turskyi.travelling.R
import ua.turskyi.travelling.features.home.view.ui.HomeActivity

inline fun <reified A : Activity> Context.start(configIntent: Intent.() -> Unit = {}) {
    startActivity(
        Intent(this, A::class.java).apply(configIntent),
    )
}

fun Context.isFacebookInstalled() = try {
    @Suppress("DEPRECATION")
    packageManager.getPackageInfo(
        getString(R.string.facebook_package),
        PackageManager.GET_META_DATA,
    )
    true
} catch (e: PackageManager.NameNotFoundException) {
    e.printStackTrace()
    false
}

fun Context.spToPix(@DimenRes sizeRes: Int): Float {
    return resources.getDimension(sizeRes) / resources.displayMetrics.density
}

fun Context.getToolbarHeight(): Int {
    val styledAttributes = theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
    val toolbarHeight = styledAttributes.getDimension(0, 0f).toInt()
    styledAttributes.recycle()
    return toolbarHeight
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

fun <T> Context.openActivityWithObject(
    destination: Class<T>,
    argId: String,
    extra: Parcelable
) {
    val intent = Intent(this, destination)
    intent.putExtra(argId, extra)
    startActivity(intent)
}

fun <T> Context.openActivityWithArgs(
    destination: Class<T>,
    extras: Bundle.() -> Unit = {},
) {
    val intent = Intent(this, destination)
    intent.putExtras(Bundle().apply(extras))
    startActivity(intent)
}

fun Context.toast(msg: String) = Toast.makeText(
    this,
    msg,
    Toast.LENGTH_SHORT,
).show()

fun Context.toastLong(msg: String) = Toast.makeText(
    this,
    msg,
    Toast.LENGTH_LONG,
).show()

fun Context.hideKeyboard() {
    val systemService = getSystemService(Activity.INPUT_METHOD_SERVICE)
    if (systemService is InputMethodManager) {
        var view: View? = (this as? Activity)?.currentFocus
        if (view == null) {
            view = View(this)
        }
        systemService.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }
}

fun Context.showKeyboard() {
    val systemService = getSystemService(Activity.INPUT_METHOD_SERVICE)
    if (systemService is InputMethodManager) {
        var view: View? = (this as? Activity)?.currentFocus
        if (view == null) {
            view = View(this)
        }
        @Suppress("DEPRECATION")
        systemService.showSoftInput(
            view, InputMethodManager.SHOW_IMPLICIT,
        )
    }
}

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
            toast(getString(R.string.error_message_try_tomorrow))
            dialog.cancel()
            val newIntent = Intent(this, javaClass)
            startActivity(newIntent)
        }.create()
    lastDialog.show()
}

fun Context.showErrorWithReportDialog(errorMessage: String) {
    val supportEmail = getString(R.string.email)
    AlertDialog.Builder(this)
        .setTitle(getString(R.string.error_cloud_sync_title))
        .setMessage(
            getString(
                R.string.error_cloud_sync_message,
                errorMessage,
                supportEmail,
            ),
        )
        .setPositiveButton(getString(R.string.btn_report_issue)) { _, _ ->
            val intent = Intent(
                Intent.ACTION_SENDTO,
                Uri.fromParts(
                    getString(R.string.scheme_mailto),
                    supportEmail,
                    null
                )
            ).apply {
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.error_email_subject),
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    getString(
                        R.string.error_email_body,
                        errorMessage,
                        android.os.Build.MODEL,
                        android.os.Build.VERSION.SDK_INT
                    )
                )
            }
            try {
                startActivity(
                    Intent.createChooser(
                        intent,
                        getString(R.string.chooser_send_email),
                    ),
                )
            } catch (_: Exception) {
                toast(getString(R.string.msg_no_email_client))
            }
        }
        .setNegativeButton(getString(R.string.no), null)
        .show()
}
