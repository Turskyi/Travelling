package ua.turskyi.travelling.features.home.view.ui

import android.content.Context
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
import ua.turskyi.travelling.features.home.viewmodel.AddCityViewModel
import ua.turskyi.travelling.models.CityNode
import ua.turskyi.travelling.models.CountryNode
import ua.turskyi.travelling.utils.Tips
import ua.turskyi.travelling.widget.LinedEditText
import java.util.*

class AddCityDialogFragment(private val countryNode: CountryNode?) : DialogFragment() {
    companion object {
        const val DIALOG_LOG = "===="
    }
    private val viewModel by inject<AddCityViewModel>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

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
            checkIfGpsEnabled(editText)
        }
        return alertDialog!!
    }

    private fun checkIfGpsEnabled(editText: LinedEditText) {
        val (gpsEnabled, networkEnabled) = checkIfGpsEnabled()
        if (!gpsEnabled && !networkEnabled) {
            Tips.show(getString(R.string.dialogue_turn_on_gps))
            Log.d(DIALOG_LOG, "GPS not Enabled")
        } else {
            Log.d(DIALOG_LOG, "GPS Enabled")
            addCityTo(editText)
        }
    }

    private fun checkIfGpsEnabled(): Pair<Boolean, Boolean> {
        var gpsEnabled = false
        var networkEnabled = false
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)!!
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(DIALOG_LOG, e.message!!)
        }
        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)!!
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(DIALOG_LOG, e.message!!)
        }
        return Pair(gpsEnabled, networkEnabled)
    }

    private fun addCityTo(editText: LinedEditText) {
        val findLastLocationTask = fusedLocationClient.lastLocation
        findLastLocationTask.addOnSuccessListener { location ->
            if (location != null) {
                addLastLocation(location, editText)
            } else {
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
                    Log.d(DIALOG_LOG, "location changed $cityChanged")
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
        Log.d(DIALOG_LOG, "findLastLocationTask is successful and location not null")
        val geoCoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses: MutableList<Address>? =
            location.latitude.let { latitude ->
                geoCoder.getFromLocation(
                    latitude,
                    location.longitude, 1
                )
            }
        val cityName: String? = addresses?.get(0)?.locality
        Log.d(DIALOG_LOG, "last location: $cityName")
        editText.setText(cityName)
    }
}