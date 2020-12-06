package ua.turskyi.travelling.features.home.view.ui

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import ua.turskyi.travelling.R
import ua.turskyi.travelling.common.Constants.ACCESS_LOCATION_AND_EXTERNAL_STORAGE
import ua.turskyi.travelling.common.Constants.TIME_INTERVAL
import ua.turskyi.travelling.databinding.ActivityHomeBinding
import ua.turskyi.travelling.decoration.SectionAverageGapItemDecoration
import ua.turskyi.travelling.extensions.*
import ua.turskyi.travelling.features.allcountries.view.ui.AllCountriesActivity
import ua.turskyi.travelling.features.flags.view.FlagsActivity
import ua.turskyi.travelling.features.flags.view.FlagsActivity.Companion.EXTRA_POSITION
import ua.turskyi.travelling.features.home.view.adapter.HomeAdapter
import ua.turskyi.travelling.features.home.viewmodels.HomeActivityViewModel
import ua.turskyi.travelling.models.City
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.models.VisitedCountry
import ua.turskyi.travelling.utils.BillingManager
import ua.turskyi.travelling.utils.PermissionHandler
import ua.turskyi.travelling.utils.PermissionHandler.isPermissionGranted
import ua.turskyi.travelling.utils.PermissionHandler.requestPermission
import java.util.*
import kotlin.concurrent.schedule
import kotlin.coroutines.CoroutineContext

class HomeActivity : AppCompatActivity(), CoroutineScope, DialogInterface.OnDismissListener,
    SyncDialog.SyncListener {

    private lateinit var binding: ActivityHomeBinding

    private lateinit var billingManager: BillingManager
    private lateinit var authorizationResultLauncher: ActivityResultLauncher<Intent>
    private var backPressedTiming: Long = 0
    private var mLastClickTime: Long = 0
    private val viewModel by inject<HomeActivityViewModel>()
    private val homeAdapter by inject<HomeAdapter>()

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        registerAuthorization()
        PermissionHandler.checkPermission(this@HomeActivity)
        billingManager = BillingManager(this@HomeActivity)
        initListeners()
    }

    override fun onResume() {
        super.onResume()
        /* makes info icon visible */
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        launch { viewModel.initListOfCountries() }

        binding.circlePieChart.animatePieChart()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (!viewModel.isSynchronized) {
            /* makes sync icon visible */
            val inflater = menuInflater
            inflater.inflate(R.menu.menu_sync, menu)
        }
        return true
    }

    /**
     * Calling when "add city dialogue" dismissed.
     */
    override fun onDismiss(dialogInterface: DialogInterface?) {
        launch { viewModel.initListOfCountries() }
    }

    /**
     * Calling when user clicks "ok" button in "sync dialogue".
     */
    override fun initSynchronization() {
        /** faking billing query */
//        TODO:remove before uploading on play market
        Timer().schedule(2000) {
            setUpgradedVersion()
        }
        /*___________________*/

        billingManager.launchBilling()
    }

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
            R.id.action_sync -> {
                if (viewModel.isUpgraded) {
                    viewModel.syncDatabaseWithFireStore()
                } else {
                    openSyncDialog(R.string.txt_info_billing)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResult: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResult)
        when (requestCode) {
            ACCESS_LOCATION_AND_EXTERNAL_STORAGE -> {
                if ((grantResult.isNotEmpty() && grantResult[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    isPermissionGranted = true
                    initView()
                    initObservers()
                } else {
                    requestPermission(this)
                }
            }
        }
    }

    fun initView() {
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
                /* mis-clicking prevention, using threshold of 1000 ms */
                if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
                    openActivityWithArgs(FlagsActivity::class.java) {
                        putInt(EXTRA_POSITION, getItemPosition(country))
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
        /*  here could be a more efficient way to handle a click to open activity,
        * but it is made on purpose of demonstration databinding */
        viewModel.navigateToAllCountries.observe(this, { shouldNavigate ->
            if (shouldNavigate == true) {
                start<AllCountriesActivity>()
                viewModel.onNavigatedToAllCountries()
            }
        })
        viewModel.visitedCountriesWithCities.observe(this, { visitedCountries ->
            updateAdapterWith(visitedCountries)
            initTitleWithNumberOf(visitedCountries)
        })
        viewModel.visitedCountries.observe(this, { visitedCountries ->
            binding.circlePieChart.apply {
                initPieChart()
                createPieChartWith(visitedCountries, viewModel.notVisitedCountriesCount)
            }
            showFloatBtn(visitedCountries)
        })
        viewModel.visibilityLoader.observe(this, { currentVisibility ->
            binding.pb.visibility = currentVisibility
        })

        viewModel.errorMessage.observe(this, { event ->
            event.getMessageIfNotHandled()?.let { message ->
                toast(message)
            }
        })
    }

    fun setUpgradedVersion() = viewModel.upgradeAndSync(authorizationResultLauncher)

    fun setTitle() {
        if (viewModel.citiesCount > 0) {
            showTitleWithCitiesAndCountries()
        } else {
            showTitleWithOnlyCountries()
        }
    }

    private fun registerAuthorization() {
        authorizationResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val response = IdpResponse.fromResultIntent(result.data)
            /* Successfully signed in */
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.syncDatabaseWithFireStore()
            } else {
                /* Sign in failed */
                when {
                    response == null -> {
                        /* User pressed back button */
                        showSnackbar(R.string.msg_sign_in_cancelled)
                        return@registerForActivityResult
                    }
                    response.error?.errorCode == ErrorCodes.NO_NETWORK -> {
                        showSnackbar(R.string.msg_no_internet)
                        return@registerForActivityResult
                    }
                    else -> {
                        toast(R.string.msg_did_not_sign_in)
                    }
                }
            }
        }
    }

    private fun initGravityForTitle() {
        if (getScreenWidth() < 1082) binding.toolbarLayout.expandedTitleGravity = Gravity.BOTTOM
    }

    private fun removeCityOnLongClick(city: City) = viewModel.removeCity(city)

    private fun showFloatBtn(visitedCountries: List<Country>?) {
        if (visitedCountries.isNullOrEmpty()) {
            binding.floatBtnLarge.show()
            binding.floatBtnSmall.visibility = View.GONE
        } else {
            binding.floatBtnLarge.hide()
            binding.floatBtnSmall.show()
        }
    }

    private fun updateAdapterWith(visitedCountries: List<VisitedCountry>) {
        for (countryNode in visitedCountries) {
            countryNode.isExpanded = false
        }
        homeAdapter.setList(visitedCountries)
    }

    private fun initTitleWithNumberOf(visitedCountries: List<VisitedCountry>) {
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

    fun showTitleWithOnlyCountries() {
        viewModel.visitedCountriesWithCities.observe(this, { countryList ->
            binding.toolbarLayout.title = resources.getQuantityString(
                R.plurals.numberOfCountriesVisited,
                countryList.size,
                countryList.size
            )
        })
    }
}