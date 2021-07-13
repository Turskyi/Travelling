package ua.turskyi.travelling.utils.extensions

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.facebook.share.model.ShareHashtag
import com.facebook.share.model.ShareMediaContent
import com.facebook.share.model.SharePhoto
import com.facebook.share.widget.ShareDialog
import com.google.android.material.snackbar.Snackbar
import ua.turskyi.travelling.R
import ua.turskyi.travelling.common.Constants
import java.text.SimpleDateFormat
import java.util.*

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

fun View.toast(@StringRes msgResId: Int) = context.toast(msgResId)

fun View.shareImageViaChooser() {
    val fileName =
        "piechart${SimpleDateFormat(context.getString(R.string.day_month_year), Locale.ENGLISH).format(Date())}.jpg"
    val bitmap = getScreenShot()
    val file = bitmap?.convertBitmapToFile(context, fileName)
    val uri = file?.let { screenShootFile ->
        FileProvider.getUriForFile(
            context,
            context.packageName.toString() + ".provider",
            screenShootFile
        )
    }

    val intentImage = Intent()
    intentImage.action = Intent.ACTION_SEND
    intentImage.type = "image/*"
    intentImage.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intentImage.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name)
    intentImage.putExtra(
        Intent.EXTRA_TEXT,
        "#travelling_the_world \n ${Constants.GOOGLE_PLAY_ADDRESS}"
    )
    intentImage.putExtra(Intent.EXTRA_STREAM, uri)
    try {
        context.startActivity(
            Intent.createChooser(
                intentImage,
                context.getString(R.string.share_title)
            )
        )
    } catch (e: ActivityNotFoundException) {
        toast(R.string.msg_no_app_installed)
    }
}


fun View.shareViaFacebook(fragment: Fragment) {
    val webAddress =
        ShareHashtag.Builder()
            .setHashtag("#travelling_the_world \n ${Constants.GOOGLE_PLAY_ADDRESS}")
            .build()
    val bitmap = getScreenShot()
    val sharePhoto = SharePhoto.Builder().setBitmap(bitmap).setCaption(
        "piechart${
            SimpleDateFormat(
                context.getString(R.string.day_month_year),
            Locale.ENGLISH
        ).format(Date())}"
    )
        .build()
    val mediaContent = ShareMediaContent.Builder()
        .addMedium(sharePhoto)
        .setShareHashtag(webAddress)
        .build()
    ShareDialog.show(fragment, mediaContent)
}

