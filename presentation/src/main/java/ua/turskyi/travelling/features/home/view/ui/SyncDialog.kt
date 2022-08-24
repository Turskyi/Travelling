package ua.turskyi.travelling.features.home.view.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import ua.turskyi.travelling.R

class SyncDialog : AppCompatDialogFragment() {
    companion object {
        const val ARG_INFO = "ua.turskyi.travelling.ARG_INFO"

        fun newInstance(info: String): SyncDialog {
            val fragment = SyncDialog()
            val bundle = Bundle().apply { putString(ARG_INFO, info) }
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var listener: SyncListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = try {
            context as SyncListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + "must implement SyncListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        builder.setMessage(arguments?.getString(ARG_INFO))
            .setPositiveButton(getString(R.string.dialog_btn_ok_ready)) { _, _ ->
                listener.showTravellingPro()
            }.setNegativeButton(getString(R.string.dialog_btn_not_ok)) { _, _ ->
                dismiss()
            }
        return builder.create()
    }

    interface SyncListener {
        fun showTravellingPro()
    }
}