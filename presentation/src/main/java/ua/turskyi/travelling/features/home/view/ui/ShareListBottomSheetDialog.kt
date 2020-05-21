package ua.turskyi.travelling.features.home.view.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import com.facebook.share.model.ShareHashtag
import com.facebook.share.model.ShareMediaContent
import com.facebook.share.model.SharePhoto
import com.facebook.share.widget.ShareDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.layout_bottom_sheet.*
import splitties.toast.toast
import ua.turskyi.travelling.Constant
import ua.turskyi.travelling.R
import ua.turskyi.travelling.extensions.mapBitmapToFile
import ua.turskyi.travelling.extensions.mapViewToBitmap
import ua.turskyi.travelling.utils.Tips
import java.text.SimpleDateFormat
import java.util.*

class ShareListBottomSheetDialog : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.BottomSheetMenuTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        ivFacebook.setOnClickListener {
            if (isFacebookInstalled(requireContext())) {
                shareViaFacebook()
            } else {
                Tips.show("You do not have Facebook app installed")
            }
            dismiss()
        }
        ivOther.setOnClickListener {
            shareImageViaChooser()
            dismiss()
        }
    }

    private fun shareImageViaChooser() {
        val fileName =
            "piechart${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())}.jpg"
        val bitmap = getScreenShot((activity as HomeActivity).toolbarLayout)
        val file = bitmap?.mapBitmapToFile(requireContext(), fileName)
        val uri = file?.let {
            FileProvider.getUriForFile(
                requireContext(),
                requireContext().packageName.toString() + ".provider",
                it
            )
        }

        val intentImage = Intent()
        intentImage.action = Intent.ACTION_SEND
        intentImage.type = "image/*"
        intentImage.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intentImage.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name)
        intentImage.putExtra(Intent.EXTRA_TEXT, "#travelling_the_world \n ${Constant.GOOGLE_PLAY_ADDRESS}")
        intentImage.putExtra(Intent.EXTRA_STREAM, uri)
        try {
            startActivity(
                Intent.createChooser(
                    intentImage,
                    getString(R.string.share_title)
                )
            )
        } catch (e: ActivityNotFoundException) {
            toast("No app available to share pie chart")
        }
    }

    private fun getScreenShot(view: View): Bitmap? {
        return view.mapViewToBitmap()?.let { Bitmap.createBitmap(it) }
    }

    private fun isFacebookInstalled(context: Context): Boolean {
        val packageManager: PackageManager = context.packageManager
        try {
            packageManager.getPackageInfo("com.facebook.katana", PackageManager.GET_META_DATA)
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
        return true
    }

    private fun shareViaFacebook() {
        val webAddress =
            ShareHashtag.Builder()
                .setHashtag("#travelling_the_world \n ${Constant.GOOGLE_PLAY_ADDRESS}")
                .build()
        val bitmap = getScreenShot((activity as HomeActivity).toolbarLayout)
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
}