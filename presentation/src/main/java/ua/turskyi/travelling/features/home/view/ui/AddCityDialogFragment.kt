package ua.turskyi.travelling.features.home.view.ui

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.location.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.koin.android.ext.android.inject
import ua.turskyi.travelling.R
import ua.turskyi.travelling.features.home.viewmodels.AddCityViewModel
import ua.turskyi.travelling.models.City
import ua.turskyi.travelling.models.VisitedCountry
import ua.turskyi.travelling.utils.Tips
import ua.turskyi.travelling.utils.isOnline
import ua.turskyi.travelling.widgets.LinedEditText
import java.util.*

class AddCityDialogFragment(private val visitedCountry: VisitedCountry) : DialogFragment(){

    companion object {
        const val DIALOG_LOG = "DIALOG_LOG"
        const val CITY_LOG = "CITY_LOG"
    }

    private val viewModel by inject<AddCityViewModel>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        initLocationServices()

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

        initListeners(buttonSave, editText, visitedCountry, alertDialog, buttonGps)
        return alertDialog!!
    }

    private fun initLocationServices() {
        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private fun initListeners(
        buttonSave: Button,
        editText: LinedEditText,
        visitedCountry: VisitedCountry,
        alertDialog: AlertDialog?,
        buttonGps: Button
    ) {
        buttonSave.setOnClickListener {
            if (editText.text.toString() != "") {
                viewModel.insert(
                    City(editText.text.toString(), visitedCountry.id)
                )
            } else {
                alertDialog?.cancel()
            }
            alertDialog?.dismiss()
        }

        buttonGps.setOnClickListener {
            checkIfGpsEnabled(editText)
        }
    }

    private fun checkIfGpsEnabled(editText: LinedEditText) {
        val gpsEnabled = checkIfGpsEnabled()
        if (!gpsEnabled) {
            Tips.show(getString(R.string.dialogue_turn_on_gps))
        } else if (!isOnline()) {
            Tips.show(getString(R.string.dialog_turn_no_internet))
        } else {
            addCityTo(editText)
        }
    }

    private fun checkIfGpsEnabled():Boolean {
        var gpsEnabled = false
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return gpsEnabled
    }

    private fun addCityTo(editText: LinedEditText) {
        val findLastLocationTask = fusedLocationClient.lastLocation
        findLastLocationTask.addOnSuccessListener { location ->
            if (location != null) {
                addLastLocation(location, editText)
            } else {
                Tips.show(getString(R.string.dialog_hold_on))
                addChangedLocation(editText)
            }
        }
    }

    private fun addChangedLocation(editText: LinedEditText) {
        try {
            val locationListener: LocationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val geoCoder = Geocoder(requireContext(), Locale.getDefault())
                    val addressesChanged: MutableList<Address>? =
                        location.latitude.let { latitude ->
                            geoCoder.getFromLocation(
                                latitude,
                                location.longitude, 1
                            )
                        }
                    val cityChanged: String? = addressesChanged?.get(0)?.locality
                    editText.setText(cityChanged)
                }

                override fun onStatusChanged(
                    provider: String,
                    status: Int,
                    extras: Bundle
                ) {
                }

                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
            /*      Request location updates */
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0L,
                0f,
                locationListener
            )
        } catch (ex: SecurityException) {
            Log.d(DIALOG_LOG, "Security Exception, no location available ${ex.message}")
        }
    }

    private fun addLastLocation(
        location: Location,
        editText: LinedEditText
    ) {
        val geoCoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses: MutableList<Address>? =
            location.latitude.let { latitude ->
                geoCoder.getFromLocation(
                    latitude,
                    location.longitude, 1
                )
            }
        val cityName: String? = addresses?.get(0)?.locality
        editText.setText(cityName)
    }
  override  fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val activity: Activity? = activity
        if (activity is DialogInterface.OnDismissListener) {
            (activity as DialogInterface.OnDismissListener).onDismiss(dialog)
        }
    }
}