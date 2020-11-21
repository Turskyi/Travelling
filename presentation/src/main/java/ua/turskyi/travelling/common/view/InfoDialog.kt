package ua.turskyi.travelling.common.view

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import ua.turskyi.travelling.R
import ua.turskyi.travelling.extensions.getHomeActivity

class InfoDialog : AppCompatDialogFragment() {
    companion object {
        const val ARG_INFO = "ua.turskyi.travelling.ARG_INFO"
        const val ARG_ACTION = "ua.turskyi.travelling.ARG_ACTION"

        fun newInstance(info: String, action: Boolean): InfoDialog {
            val fragment = InfoDialog()
            val bundle = Bundle().apply {
                putString(ARG_INFO, info)
                putBoolean(ARG_ACTION, action)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setMessage(arguments?.getString(ARG_INFO))
            .setPositiveButton(getString(R.string.dialog_btn_ok)) { _, _ ->
                if (arguments?.getBoolean(ARG_ACTION) == true) {
                   context?.getHomeActivity()?.launchBilling()
                }
            }
        return builder.create()
    }
}