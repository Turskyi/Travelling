package ua.turskyi.travelling.extensions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.annotation.DimenRes
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_home.*
import splitties.toast.toast
import ua.turskyi.travelling.Constants
import ua.turskyi.travelling.R
import ua.turskyi.travelling.features.home.view.ui.HomeActivity
import java.text.SimpleDateFormat
import java.util.*

fun Context.isFacebookInstalled(): Boolean {
    try {
        packageManager.getPackageInfo("com.facebook.katana", PackageManager.GET_META_DATA)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        return false
    }
    return true
}

fun Context.spToPix(@DimenRes sizeRes: Int) =
    resources.getDimension(sizeRes) / resources.displayMetrics.density

fun Context.shareImageViaChooser() {
    val fileName =
        "piechart${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())}.jpg"
    val bitmap = (this as HomeActivity).toolbarLayout.getScreenShot()
    val file = bitmap?.convertBitmapToFile(this, fileName)
    val uri = file?.let {
        FileProvider.getUriForFile(
            this,
            this.packageName.toString() + ".provider",
            it
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
        startActivity(
            Intent.createChooser(
                intentImage,
                getString(R.string.share_title)
            )
        )
    } catch (e: ActivityNotFoundException) {
        toast(getString(R.string.toast_no_app_installed))
    }
}