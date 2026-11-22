package ua.turskyi.travelling.features.home.view.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ua.turskyi.domain.interactor.AuthInteractor
import ua.turskyi.domain.interactor.CloudSyncInteractor
import ua.turskyi.travelling.R
import ua.turskyi.travelling.utils.extensions.showErrorWithReportDialog

class SyncDialog : AppCompatDialogFragment(), KoinComponent {

    var isTravelersFlow: Boolean = false

    companion object {
        private const val TAG = "SyncDialog"
        private const val ARG_IS_TRAVELERS = "arg_is_travelers"
        fun newInstance(isTravelersFlow: Boolean = false): SyncDialog {
            return SyncDialog().apply {
                this.isTravelersFlow = isTravelersFlow
                arguments = Bundle().apply {
                    putBoolean(ARG_IS_TRAVELERS, isTravelersFlow)
                }
            }
        }
    }

    private val authInteractor: AuthInteractor by inject()
    private val cloudSyncInteractor: CloudSyncInteractor by inject()

    private lateinit var syncListener: SyncListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        syncListener = try {
            context as SyncListener
        } catch (_: ClassCastException) {
            object : SyncListener {
                override fun showTravellingPro() {}
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val user = authInteractor.getCurrentUser()
        Log.d(TAG, "onCreateDialog: user = $user, isTravelersFlow = $isTravelersFlow")
        if (user == null) {
            return buildExplanationDialog()
        } else {
            // Already signed in: immediately perform bidirectional sync / cloud backup
            val builder = AlertDialog.Builder(requireActivity())
            builder.setTitle(getString(R.string.dialog_cloud_sync_title))
                .setMessage(getString(R.string.dialog_cloud_sync_message))
                .setCancelable(false)
            val dialog = builder.create()

            dialog.setOnShowListener {
                lifecycleScope.launch {
                    val syncResult = cloudSyncInteractor.performBidirectionalSync()
                    dialog.dismiss()
                    syncResult.onSuccess {
                        Toast.makeText(requireContext(), getString(R.string.msg_cloud_sync_success), Toast.LENGTH_SHORT).show()
                        (activity as? HomeActivity)?.let { home ->
                            home.refreshData()
                            if (isTravelersFlow) {
                                home.startActivity(
                                    Intent(
                                        home,
                                        ua.turskyi.travelling.features.travellers.view.TravellersActivity::class.java
                                    )
                                )
                            }
                        }
                    }.onFailure { e ->
                        requireContext().showErrorWithReportDialog(e.localizedMessage ?: e.toString())
                    }
                }
            }
            return dialog
        }
    }

    private fun buildExplanationDialog(): Dialog {
        Log.d(TAG, "buildExplanationDialog: isTravelersFlow = $isTravelersFlow")
        val view = layoutInflater.inflate(R.layout.dialog_sync_explanation, null)
        val tvMessage = view.findViewById<TextView>(R.id.tvExplanation)
        val cbKeepSignedIn = view.findViewById<CheckBox>(R.id.cbKeepSignedIn)

        tvMessage.text = if (isTravelersFlow) {
            getString(R.string.travelers_sync_explanation)
        } else {
            getString(R.string.sync_explanation)
        }
        cbKeepSignedIn.isChecked = false

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)
            .setTitle(if (isTravelersFlow) getString(R.string.travelers_dialog_title) else getString(R.string.paywall_title))
            .setPositiveButton(getString(R.string.btn_sign_in_google)) { _, _ ->
                authInteractor.setKeepSignedIn(cbKeepSignedIn.isChecked)
                syncListener.onInitiateGoogleSignIn(cbKeepSignedIn.isChecked)
            }
            .setNegativeButton(getString(R.string.home_dialog_btn_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
        return builder.create()
    }

    interface SyncListener {
        fun showTravellingPro()
        fun onInitiateGoogleSignIn(keepSignedIn: Boolean) {}
    }
}
