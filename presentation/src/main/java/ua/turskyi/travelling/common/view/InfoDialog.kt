package ua.turskyi.travelling.common.view

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment

class InfoDialog : AppCompatDialogFragment() {
    companion object {
        const val ARG_INFO = "info"

        fun newInstance(info: String): InfoDialog {
            val fragment = InfoDialog()
            val bundle = Bundle().apply {
                putString(ARG_INFO, info)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setMessage(arguments?.getString(ARG_INFO))
            .setPositiveButton("OK") { _, _ -> }
        return builder.create()
    }
}