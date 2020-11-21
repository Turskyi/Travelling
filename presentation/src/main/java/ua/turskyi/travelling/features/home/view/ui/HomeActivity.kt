package ua.turskyi.travelling.features.home.view.ui

import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import splitties.activities.start
import splitties.toast.longToast
import ua.turskyi.travelling.R
import ua.turskyi.travelling.common.Constants.ACCESS_LOCATION_AND_EXTERNAL_STORAGE
import ua.turskyi.travelling.common.Constants.TIME_INTERVAL
import ua.turskyi.travelling.common.prefs
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
import kotlin.coroutines.CoroutineContext

class HomeActivity : AppCompatActivity(), CoroutineScope, DialogInterface.OnDismissListener {

    private lateinit var binding: ActivityHomeBinding

    private lateinit var billingManager: BillingManager
    private var backPressedTiming: Long = 0
    private var mLastClickTime: Long = 0
    private val homeViewModel by inject<HomeActivityViewModel>()
    private val homeAdapter by inject<HomeAdapter>()

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        PermissionHandler.checkPermission(this@HomeActivity)
        billingManager = BillingManager(this@HomeActivity)
        initListeners()
    }

    override fun onResume() {
        super.onResume()
        /* makes info icon visible */
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        launch { homeViewModel.initListOfCountries() }

        binding.circlePieChart.animatePieChart()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (!prefs.isSynchronized) {
            /* makes sync icon visible */
            val inflater = menuInflater
            inflater.inflate(R.menu.menu_sync, menu)
        }
        return true
    }

    override fun onDismiss(p0: DialogInterface?) {
        launch { homeViewModel.initListOfCountries() }
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
                if (prefs.isUpgraded) {
                    homeViewModel.syncDatabaseWithFireStore()
                } else {
                    openInfoDialog(R.string.txt_info_billing, true)
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
        binding.viewModel = this.homeViewModel
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

    fun launchBilling() = billingManager.launchBilling()

    private fun initListeners() {
        homeAdapter.apply {
            onFlagClickListener = { country ->
                /* mis-clicking prevention, using threshold of 1000 ms */
                if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
                    openActivity(FlagsActivity::class.java) {
                        putInt(EXTRA_POSITION, getItemPosition(country))
                    }
                }
                mLastClickTime = SystemClock.elapsedRealtime()
            }
            onLongClickListener = { countryNode ->
                val country = countryNode.mapNodeToActual()

                binding.root.showSnackWithAction(getString(R.string.delete_it, country.name)) {
                    action(R.string.yes) {
                        homeViewModel.removeFromVisited(country)
                        longToast(getString(R.string.deleted, country.name))
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
                        longToast(getString(R.string.deleted, city.name))
                    }
                }
            }
        }
    }

    fun setTitle() {
        if (homeViewModel.citiesCount > 0) {
            showTitleWithCitiesAndCountries()
        } else {
            showTitleWithOnlyCountries()
        }
    }

    fun initObservers() {
        /*  here could be a more efficient way to handle a click to open activity,
        * but it is made on purpose of demonstration databinding */
        homeViewModel.navigateToAllCountries.observe(this,
            { shouldNavigate ->
                if (shouldNavigate == true) {
                    start<AllCountriesActivity>()
                    homeViewModel.onNavigatedToAllCountries()
                }
            })
        homeViewModel.visitedCountriesWithCities.observe(this, { visitedCountries ->
            updateAdapterWith(visitedCountries)
            initTitleWithNumberOf(visitedCountries)
        })
        homeViewModel.visitedCountries.observe(
            this,
            { visitedCountries ->
                binding.circlePieChart.apply {
                    initPieChart()
                    createPieChartWith(visitedCountries, homeViewModel.notVisitedCount)
                }
                showFloatBtn(visitedCountries)
            })
        homeViewModel.visibilityLoader.observe(this, { currentVisibility ->
            binding.pb.visibility = currentVisibility
        })
    }

    fun setUpgradedVersion() {
        if (!prefs.isSynchronized) {
            homeViewModel.syncDatabaseWithFireStore()
        }
        prefs.isUpgraded = true
    }

    private fun initGravityForTitle() {
        if (getScreenWidth() < 1082) binding.toolbarLayout.expandedTitleGravity = Gravity.BOTTOM
    }

    private fun removeCityOnLongClick(city: City) = homeViewModel.removeCity(city)

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
        if (homeViewModel.citiesCount == 0) {
            binding.toolbarLayout.title = resources.getQuantityString(
                R.plurals.numberOfCountriesVisited,
                visitedCountries.size,
                visitedCountries.size
            )
        } else {
            if (homeViewModel.citiesCount > visitedCountries.size) {
                binding.toolbarLayout.title = "${
                    resources.getQuantityString(
                        R.plurals.numberOfCitiesVisited,
                        homeViewModel.citiesCount,
                        homeViewModel.citiesCount
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
        homeViewModel.visitedCountriesWithCities.observe(this, { countries ->
            if (homeViewModel.citiesCount > countries.size) {
                binding.toolbarLayout.title = "${
                    resources.getQuantityString(
                        R.plurals.numberOfCitiesVisited,
                        homeViewModel.citiesCount,
                        homeViewModel.citiesCount
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
        homeViewModel.visitedCountriesWithCities.observe(this, { countryList ->
            binding.toolbarLayout.title = resources.getQuantityString(
                R.plurals.numberOfCountriesVisited,
                countryList.size,
                countryList.size
            )
        })
    }
}