package ua.turskyi.travelling.features.home.view.ui

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import ua.turskyi.travelling.R
import ua.turskyi.travelling.common.Constants.ACCESS_LOCATION_AND_EXTERNAL_STORAGE
import ua.turskyi.travelling.common.Constants.TIME_INTERVAL
import ua.turskyi.travelling.databinding.ActivityHomeBinding
import ua.turskyi.travelling.decoration.SectionAverageGapItemDecoration
import ua.turskyi.travelling.extensions.*
import ua.turskyi.travelling.features.allcountries.view.ui.AllCountriesActivity
import ua.turskyi.travelling.features.flags.view.FlagsActivity
import ua.turskyi.travelling.features.flags.view.FlagsActivity.Companion.EXTRA_ITEM_COUNT
import ua.turskyi.travelling.features.flags.view.FlagsActivity.Companion.EXTRA_POSITION
import ua.turskyi.travelling.features.home.view.adapter.HomeAdapter
import ua.turskyi.travelling.features.home.viewmodels.HomeActivityViewModel
import ua.turskyi.travelling.models.City
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.models.VisitedCountry
import ua.turskyi.travelling.utils.PermissionHandler
import ua.turskyi.travelling.utils.PermissionHandler.isPermissionGranted
import ua.turskyi.travelling.utils.PermissionHandler.requestPermission
import java.util.*

class HomeActivity : AppCompatActivity(), DialogInterface.OnDismissListener
//TODO: for future release
/*, SyncDialog.SyncListener*/ {

    //  TODO:  for future release
//    private lateinit var billingManager: BillingManager
//    private lateinit var authorizationResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var allCountriesResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivityHomeBinding

    private var backPressedTiming: Long = 0
    private var mLastClickTime: Long = 0

    private val viewModel by inject<HomeActivityViewModel>()
    private val homeAdapter by inject<HomeAdapter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)

        registerAllCountriesActivityResultLauncher()
        PermissionHandler.checkPermission(this@HomeActivity)
        initView()
//      TODO:  for future release
//        registerAuthorization()
//        billingManager = BillingManager(this@HomeActivity)
        initListeners()
    }

    override fun onResume() {
        super.onResume()
        // makes info icon visible
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

//   TODO: for future release
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        if (!viewModel.isSynchronized) {
//            /* makes sync icon visible */
//            val inflater = menuInflater
//            inflater.inflate(R.menu.menu_sync, menu)
//        }
//        return true
//    }

    /**
     * Calling when "add city dialogue" dismissed.
     */
    override fun onDismiss(dialogInterface: DialogInterface?) = viewModel.showListOfCountries()


//   TODO: for future release
//    /**
//     * Calling when user clicks "ok" button in "sync dialogue".
//     */
//    override fun initSynchronization() {
//        billingManager.launchBilling()
//    }

    override fun onBackPressed() {
        if (backPressedTiming + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed()
            return
        } else {
            binding.root.showSnackBar(R.string.tap_back_button_in_order_to_exit)
        }
        backPressedTiming = System.currentTimeMillis()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                openInfoDialog(R.string.txt_info_home)
                true
            }
//      TODO:      for future release
//            R.id.action_sync -> {
//                if (viewModel.isUpgraded) {
//                    viewModel.syncDatabaseWithFireStore()
//                } else {
//                    openSyncDialog(R.string.txt_info_billing)
//                }
//                true
//            }
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
            ACCESS_LOCATION_AND_EXTERNAL_STORAGE -> if ((grantResult.isNotEmpty()
                        && grantResult[0] == PackageManager.PERMISSION_GRANTED)
            ) {
                /** we got here the first time, when permission is received */
                isPermissionGranted = true
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
        /* set drawable icon */
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
    }

    private fun initListeners() {
        homeAdapter.apply {
            onFlagClickListener = { country ->
                // mis-clicking prevention, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
                    openActivityWithArgs(FlagsActivity::class.java) {
                        putInt(EXTRA_POSITION, getItemPosition(country))
                        viewModel.visitedCountries.value?.size?.let { itemCount ->
                            putInt(EXTRA_ITEM_COUNT, itemCount)
                        }
                    }
                }
                mLastClickTime = SystemClock.elapsedRealtime()
            }
            onLongClickListener = { countryNode ->
                val country = countryNode.mapNodeToActual()

                binding.root.showSnackWithAction(getString(R.string.delete_it, country.name)) {
                    action(R.string.yes) {
                        viewModel.removeFromVisited(country)
                        toastLong(getString(R.string.deleted, country.name))
                    }
                }
            }

            onCountryNameClickListener = { countryNode ->
                /* Creating the new Fragment with the Country id passed in. */
                val fragment = AddCityDialogFragment.newInstance(countryNode.id)
                fragment.show(supportFragmentManager, null)
            }
            onCityLongClickListener = { city ->
                binding.root.showSnackWithAction(getString(R.string.delete_it, city.name)) {
                    action(R.string.yes) {
                        removeCityOnLongClick(city)
                        toastLong(getString(R.string.deleted, city.name))
                    }
                }
            }
        }
    }

    fun initObservers() {
        viewModel.showListOfCountries()
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
                allCountriesResultLauncher.launch(Intent(this, AllCountriesActivity::class.java))
                viewModel.onNavigatedToAllCountries()
            }
        })
    }

//  TODO:  for future release
//    fun setUpgradedVersion() = viewModel.upgradeAndSync(authorizationResultLauncher)

    fun setTitle() = if (viewModel.citiesCount > 0) {
        showTitleWithCitiesAndCountries()
    } else {
        showTitleWithOnlyCountries()
    }

//  TODO:  for future release
//    private fun registerAuthorization() {
//        authorizationResultLauncher = registerForActivityResult(
//            ActivityResultContracts.StartActivityForResult()
//        ) { result ->
//            val response = IdpResponse.fromResultIntent(result.data)
//            /* Successfully signed in */
//            if (result.resultCode == Activity.RESULT_OK) {
//                viewModel.syncDatabaseWithFireStore()
//            } else {
//                /* Sign in failed */
//                when {
//                    response == null -> {
//                        /* User pressed back button */
//                        showSnackbar(R.string.msg_sign_in_cancelled)
//                        return@registerForActivityResult
//                    }
//                    response.error?.errorCode == ErrorCodes.NO_NETWORK -> {
//                        showSnackbar(R.string.msg_no_internet)
//                        return@registerForActivityResult
//                    }
//                    else -> {
//                        toast(R.string.msg_did_not_sign_in)
//                    }
//                }
//            }
//        }
//    }

    private fun registerAllCountriesActivityResultLauncher() {
        allCountriesResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                /* New Country is added to list of visited countries */
                binding.floatBtnLarge.hide()
                viewModel.showListOfCountries()
            } else {
                /* did not added country to visited list */
                when (result.resultCode) {
                    RESULT_CANCELED -> {
                        /* User pressed back button */
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

    private fun showTitleWithCitiesAndCountries() =
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

    /** must be open to use it in custom "circle pie chart" widget */
    fun showTitleWithOnlyCountries() =
        viewModel.visitedCountriesWithCities.observe(this, { countryList ->
            binding.toolbarLayout.title = resources.getQuantityString(
                R.plurals.numberOfCountriesVisited,
                countryList.size,
                countryList.size
            )
        })
}