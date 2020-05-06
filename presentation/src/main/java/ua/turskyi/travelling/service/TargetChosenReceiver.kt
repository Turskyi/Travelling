package ua.turskyi.travelling.service

import android.app.Activity
import android.app.PendingIntent
import android.content.*
import android.graphics.Bitmap
import com.facebook.share.model.ShareHashtag
import com.facebook.share.model.ShareMediaContent
import com.facebook.share.model.SharePhoto
import com.facebook.share.widget.ShareDialog
import kotlinx.android.synthetic.main.activity_home.*
import ua.turskyi.travelling.Constant.GOOGLE_PLAY_ADDRESS
import ua.turskyi.travelling.extensions.mapViewToBitmap
import ua.turskyi.travelling.features.home.view.ui.HomeActivity

/**
 * Receiver to record the chosen component when sharing an Intent.
 */
internal class TargetChosenReceiver : BroadcastReceiver() {
    companion object {
        private const val EXTRA_RECEIVER_TOKEN = "receiver_token"
        private val LOCK = Any()
        private var sTargetChosenReceiveAction: String? = null
        private var sLastRegisteredReceiver: TargetChosenReceiver? = null

        fun sendChooserIntent(activity: Activity, sharingIntent: Intent?) {
            synchronized(LOCK) {
                if (sTargetChosenReceiveAction == null) {
                    sTargetChosenReceiveAction =
                        (activity.packageName + "/"
                                + TargetChosenReceiver::class.java.name + "_ACTION")
                }
                val context: Context = activity.applicationContext
                if (sLastRegisteredReceiver != null) {
                    context.unregisterReceiver(sLastRegisteredReceiver)
                }
                sLastRegisteredReceiver = TargetChosenReceiver()
                context.registerReceiver(
                    sLastRegisteredReceiver,
                    IntentFilter(sTargetChosenReceiveAction)
                )
            }
            val intent = Intent(sTargetChosenReceiveAction)
            intent.setPackage(activity.packageName)
            intent.putExtra(
                EXTRA_RECEIVER_TOKEN,
                sLastRegisteredReceiver.hashCode()
            )
            val callback = PendingIntent.getBroadcast(
                activity, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_ONE_SHOT
            )
            val chooserIntent = Intent.createChooser(
                sharingIntent,
                "activity.getString()",
                callback.intentSender
            )
            activity.startActivity(chooserIntent)
        }
    }

    private fun shareViaFacebook() {
        val webAddress =
            ShareHashtag.Builder().setHashtag("#travelling_the_world \n $GOOGLE_PLAY_ADDRESS")
                .build()
        val bitmap = HomeActivity().toolbarLayout.mapViewToBitmap()?.let { Bitmap.createBitmap(it) }
        val sharePhoto = SharePhoto.Builder().setBitmap(bitmap).setCaption("Travelling")
            .build()
        val mediaContent = ShareMediaContent.Builder()
            .addMedium(sharePhoto)
            .setShareHashtag(webAddress)
            .build()
        ShareDialog.show(HomeActivity(), mediaContent)
    }

    override fun onReceive(context: Context, intent: Intent) {

        synchronized(LOCK) {
            if (sLastRegisteredReceiver !== this) return
            context.applicationContext
                .unregisterReceiver(sLastRegisteredReceiver)
            sLastRegisteredReceiver = null
        }
        if (!intent.hasExtra(EXTRA_RECEIVER_TOKEN)
            || intent.getIntExtra(
                EXTRA_RECEIVER_TOKEN,
                0
            ) != this.hashCode()
        ) {
            return
        }
        val target =
            intent.getParcelableExtra<ComponentName>(Intent.EXTRA_CHOSEN_COMPONENT)
        if (target != null && target.packageName.contains("com.facebook.katana")) {
            shareViaFacebook()
        } else {
            return
        }
    }
}