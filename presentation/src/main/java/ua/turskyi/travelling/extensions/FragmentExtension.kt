package ua.turskyi.travelling.extensions

import androidx.fragment.app.Fragment
import com.facebook.share.model.ShareHashtag
import com.facebook.share.model.ShareMediaContent
import com.facebook.share.model.SharePhoto
import com.facebook.share.widget.ShareDialog
import kotlinx.android.synthetic.main.activity_home.*
import ua.turskyi.travelling.common.Constants
import ua.turskyi.travelling.features.home.view.ui.HomeActivity
import java.text.SimpleDateFormat
import java.util.*

fun Fragment.shareViaFacebook() {
    val webAddress =
        ShareHashtag.Builder()
            .setHashtag("#travelling_the_world \n ${Constants.GOOGLE_PLAY_ADDRESS}")
            .build()
    val bitmap = (this.activity as HomeActivity).toolbarLayout.getScreenShot()
    val sharePhoto = SharePhoto.Builder().setBitmap(bitmap).setCaption(
        "piechart${SimpleDateFormat(
            "dd.MM.yyyy",
            Locale.getDefault()
        ).format(Date())}"
    )
        .build()
    val mediaContent = ShareMediaContent.Builder()
        .addMedium(sharePhoto)
        .setShareHashtag(webAddress)
        .build()
    ShareDialog.show(this, mediaContent)
}