package ua.turskyi.travelling.features.home.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import org.koin.android.ext.android.inject
import ua.turskyi.travelling.R
import ua.turskyi.travelling.features.home.viewmodel.AddCityViewModel
import ua.turskyi.travelling.models.CityNode
import ua.turskyi.travelling.models.CountryNode
import ua.turskyi.travelling.widget.LinedEditText

class AddCityDialogFragment(private val countryNode: CountryNode?) : DialogFragment() {

    private val viewModel by inject<AddCityViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {

        val builder = context?.let { context ->
            AlertDialog.Builder(
                context,
                R.style.RoundShapedDarkAlertDialogStyle
            )
        }

        val viewGroup = (activity as AppCompatActivity)
            .findViewById<ViewGroup>(android.R.id.content)
        val dialogView = LayoutInflater.from(context)
            .inflate(
                R.layout.dialogue_city, viewGroup,
                false
            )

        builder?.setView(dialogView)
        val alertDialog = builder?.create()

        val buttonSave = dialogView.findViewById<Button>(R.id.buttonSave)
        val buttonGps = dialogView.findViewById<Button>(R.id.btnGps)
        val editText = dialogView.findViewById<LinedEditText>(R.id.letCity)

        editText.visibility = VISIBLE
        editText.setText("")
        buttonSave.visibility = VISIBLE
        buttonGps.visibility = VISIBLE

        buttonSave.setOnClickListener {
            if (editText.text.toString() != "") {
                countryNode?.let { viewModel.addCityToCountry(it, CityNode(editText.text.toString())) }
            } else {
                alertDialog?.cancel()
            }
            alertDialog?.dismiss()
        }

        buttonGps.setOnClickListener {
//           TODO: implement add city from gps
        }
        return alertDialog!!
    }
}