package ua.turskyi.travelling.features.home.view.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import ua.turskyi.travelling.R
import ua.turskyi.travelling.databinding.ActivityHomeBinding
import ua.turskyi.travelling.decoration.SectionAverageGapItemDecoration
import ua.turskyi.travelling.features.allcountries.view.ui.AllCountriesActivity
import ua.turskyi.travelling.features.flags.view.FlagsActivity
import ua.turskyi.travelling.features.flags.view.FlagsActivity.Companion.EXTRA_ITEM_COUNT
import ua.turskyi.travelling.features.flags.view.FlagsActivity.Companion.EXTRA_POSITION
import ua.turskyi.travelling.features.home.view.adapter.HomeAdapter
import ua.turskyi.travelling.features.home.viewmodels.HomeActivityViewModel
import ua.turskyi.travelling.models.City
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.models.VisitedCountry
import ua.turskyi.travelling.utils.extensions.*
import ua.turskyi.travelling.widgets.CirclePieChart
import java.util.*

class HomeActivity : AppCompatActivity(), DialogInterface.OnDismissListener,
    SyncDialog.SyncListener {
    private val viewModel by inject<HomeActivityViewModel>()
    private val homeAdapter by inject<HomeAdapter>()

    private lateinit var binding: ActivityHomeBinding
    private lateinit var allCountriesResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        registerAllCountriesActivityResultLauncher()
        checkPermission()
        initView()
        initListeners()
        initObservers()
    }

    override fun onResume() {
        super.onResume()
        // makes info icon visible
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // makes sync icon visible
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_sync, menu)
        return true
    }

    /**
     * Calling when "add city dialogue" dismissed.
     */
    override fun onDismiss(dialogInterface: DialogInterface) {
        viewModel.showListOfVisitedCountries()
    }

    /**
     * Calling when user clicks "ok" button in "sync dialogue".
     */
    override fun showTravellingPro() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(resources.getString(R.string.play_market_app_link_to_pro_version))
                )
            )
        } catch (e: ActivityNotFoundException) {
            toast(e.localizedMessage ?: e.stackTraceToString())
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(resources.getString(R.string.play_market_web_link_to_pro_version))
                )
            )
        }
    }

    override fun onBackPressed() {
        if (viewModel.backPressedTiming + resources.getInteger(R.integer.desired_time_interval) > System.currentTimeMillis()) {
            super.onBackPressed()
            return
        } else {
            binding.root.showSnackBar(R.string.tap_back_button_in_order_to_exit)
        }
        viewModel.backPressedTiming = System.currentTimeMillis()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                openInfoDialog(R.string.txt_info_home)
                true
            }
            R.id.action_sync -> {
                val infoDialog: SyncDialog = SyncDialog.newInstance(
                    getString(R.string.txt_info_billing)
                )
                infoDialog.show(this.supportFragmentManager, "sync dialog")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResult: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResult)
        when (requestCode) {
            resources.getInteger(R.integer.location_and_storage_request_code) -> if ((grantResult.isNotEmpty()
                        && grantResult.first() == PackageManager.PERMISSION_GRANTED)
            ) {
                // we got here the first time, when permission is received
                viewModel.isPermissionGranted = true
                initObservers()
            } else {
                requestPermission(this)
            }
        }
    }

    private fun initView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this
        setSupportActionBar(binding.toolbar)
        // set drawable icon
        supportActionBar?.setHomeAsUpIndicator(R.drawable.btn_info_ripple)

        val linearLayoutManager = LinearLayoutManager(this)
        binding.rvVisitedCountries.apply {
            adapter = homeAdapter
            layoutManager = linearLayoutManager
            addItemDecoration(
                SectionAverageGapItemDecoration(
                    resources.getDimensionPixelOffset(R.dimen.offset_10),
                    resources.getDimensionPixelOffset(R.dimen.offset_10),
                    resources.getDimensionPixelOffset(R.dimen.offset_20),
                    resources.getDimensionPixelOffset(R.dimen.offset_16)
                )
            )
        }
        initGravityForTitle()
        viewModel.showListOfVisitedCountries()
    }

    private fun initListeners() {
        homeAdapter.apply {
            onFlagClickListener = { country ->
                // mis-clicking prevention, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - viewModel.mLastClickTime > resources.getInteger(
                        R.integer.click_interval
                    )
                ) {
                    openActivityWithArgs(FlagsActivity::class.java) {
                        putInt(EXTRA_POSITION, getItemPosition(country))
                        viewModel.visitedCountries.value?.size?.let { itemCount ->
                            putInt(EXTRA_ITEM_COUNT, itemCount)
                        }
                    }
                }
                viewModel.mLastClickTime = SystemClock.elapsedRealtime()
            }
            onLongClickListener = { countryNode ->
                val country: Country = countryNode.mapNodeToActual()

                binding.root.showSnackWithAction(getString(R.string.delete_it, country.name)) {
                    action(R.string.yes) {
                        viewModel.removeFromVisited(country)
                        toastLong(getString(R.string.deleted, country.name))
                    }
                }
            }

            onCountryNameClickListener = { countryNode ->
                // Showing the new Dialog Fragment with the Country id passed in.
                val fragment: AddCityDialogFragment = AddCityDialogFragment.newInstance(
                    countryNode.id,
                )
                fragment.show(supportFragmentManager, null)
            }
            onCityLongClickListener = { city: City ->
                binding.root.showSnackWithAction(getString(R.string.delete_it, city.name)) {
                    action(R.string.yes) {
                        removeCityOnLongClick(city)
                        toastLong(getString(R.string.deleted, city.name))
                    }
                }
            }
        }
    }

    private fun initObservers() {
        viewModel.visitedCountriesWithCities.observe(this, { visitedCountries ->
            initTitleWithNumberOf(visitedCountries)
            updateAdapterWith(visitedCountries)
        })
        viewModel.visitedCountries.observe(this, { visitedCountries ->
            binding.circlePieChart.apply {
                initPieChart()
                createPieChartWith(visitedCountries, viewModel.notVisitedCountriesCount)
                binding.circlePieChart.animatePieChart()
            }
            showFloatBtn(visitedCountries)
        })
        viewModel.visibilityLoader.observe(this, { currentVisibility ->
            binding.pb.visibility = currentVisibility
        })

        viewModel.errorMessage.observe(this, { event ->
            event.getMessageIfNotHandled()?.let { message ->
                toastLong(message)
            }
        })
        /*  here could be a more efficient way to handle a click to open activity,
         * but it is made on purpose of demonstration databinding */
        viewModel.navigateToAllCountries.observe(this, { shouldNavigate ->
            if (shouldNavigate == true) {
                allCountriesResultLauncher.launch(
                    Intent(this, AllCountriesActivity::class.java),
                )
                viewModel.onNavigatedToAllCountries()
            }
        })
    }

    /** [setTitle] must be open since it is used in [CirclePieChart]*/
    fun setTitle() = if (viewModel.citiesCount > 0) {
        showTitleWithCitiesAndCountries()
    } else {
        showTitleWithOnlyCountries()
    }

    private fun registerAllCountriesActivityResultLauncher() {
        allCountriesResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                // New Country is added to list of visited countries
                binding.floatBtnLarge.hide()
                viewModel.showListOfVisitedCountries()
            } else {
                // did not added country to visited list
                when (result.resultCode) {
                    RESULT_CANCELED -> {
                        // User pressed back button
                        toast(R.string.msg_home_country_did_not_added)
                        return@registerForActivityResult
                    }
                }
            }
        }
    }

    private fun initGravityForTitle() {
        if (getScreenWidth() < 1082) binding.toolbarLayout.expandedTitleGravity = Gravity.BOTTOM
    }

    private fun removeCityOnLongClick(city: City) = viewModel.removeCity(city)

    private fun showFloatBtn(visitedCountries: List<Country>?) =
        if (visitedCountries.isNullOrEmpty()) {
            binding.floatBtnLarge.show()
            binding.floatBtnSmall.visibility = View.GONE
        } else {
            binding.floatBtnLarge.hide()
            binding.floatBtnSmall.show()
        }

    private fun updateAdapterWith(visitedCountries: List<VisitedCountry>) {
        for (countryNode in visitedCountries) {
            countryNode.isExpanded = false
        }
        homeAdapter.setList(visitedCountries)
    }

    private fun initTitleWithNumberOf(visitedCountries: List<VisitedCountry>) =
        if (viewModel.citiesCount == 0) {
            binding.toolbarLayout.title = resources.getQuantityString(
                R.plurals.numberOfCountriesVisited,
                visitedCountries.size,
                visitedCountries.size
            )
        } else {
            if (viewModel.citiesCount > visitedCountries.size) {
                binding.toolbarLayout.title = "${
                    resources.getQuantityString(
                        R.plurals.numberOfCitiesVisited,
                        viewModel.citiesCount,
                        viewModel.citiesCount
                    )
                } ${
                    resources.getQuantityString(
                        R.plurals.numberOfCountriesOfCitiesVisited, visitedCountries.size,
                        visitedCountries.size
                    )
                }"
            } else {
                binding.toolbarLayout.title = "${
                    resources.getQuantityString(
                        R.plurals.numberOfCitiesVisited,
                        visitedCountries.size,
                        visitedCountries.size
                    )
                } ${
                    resources.getQuantityString(
                        R.plurals.numberOfCountriesOfCitiesVisited, visitedCountries.size,
                        visitedCountries.size
                    )
                }"
            }
        }

    private fun showTitleWithCitiesAndCountries() {
        viewModel.visitedCountriesWithCities.observe(this, { countries ->
            if (viewModel.citiesCount > countries.size) {
                binding.toolbarLayout.title = "${
                    resources.getQuantityString(
                        R.plurals.numberOfCitiesVisited,
                        viewModel.citiesCount,
                        viewModel.citiesCount
                    )
                } ${
                    resources.getQuantityString(
                        R.plurals.numberOfCountriesOfCitiesVisited, countries.size,
                        countries.size
                    )
                }"
            } else {
                binding.toolbarLayout.title = "${
                    resources.getQuantityString(
                        R.plurals.numberOfCitiesVisited,
                        countries.size,
                        countries.size
                    )
                } ${
                    resources.getQuantityString(
                        R.plurals.numberOfCountriesOfCitiesVisited, countries.size,
                        countries.size
                    )
                }"
            }
        })
    }

    /** [showTitleWithOnlyCountries] must be open to use it in custom "circle pie chart" widget */
    fun showTitleWithOnlyCountries() {
        viewModel.visitedCountriesWithCities.observe(this, { countryList ->
            binding.toolbarLayout.title = resources.getQuantityString(
                R.plurals.numberOfCountriesVisited,
                countryList.size,
                countryList.size
            )
        })
    }

    private fun checkPermission() {
        val locationPermission: Int = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val externalStoragePermission: Int =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        if (locationPermission != PackageManager.PERMISSION_GRANTED
            && externalStoragePermission != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission(this)
        } else {
            /* we are getting here every time except the first time,
             * since permission is already received */
            viewModel.isPermissionGranted = true
        }
    }

    private fun requestPermission(activity: AppCompatActivity) = ActivityCompat.requestPermissions(
        activity,
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).toTypedArray(),
        resources.getInteger(R.integer.location_and_storage_request_code)
    )
}