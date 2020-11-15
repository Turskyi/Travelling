package ua.turskyi.travelling.features.home.view.ui

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.*
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import splitties.activities.start
import splitties.toast.longToast
import splitties.toast.toast
import ua.turskyi.travelling.R
import ua.turskyi.travelling.common.Constants.ACCESS_LOCATION_AND_EXTERNAL_STORAGE
import ua.turskyi.travelling.common.Constants.SKU_ID
import ua.turskyi.travelling.common.Constants.TIME_INTERVAL
import ua.turskyi.travelling.common.prefs
import ua.turskyi.travelling.common.view.InfoDialog
import ua.turskyi.travelling.databinding.ActivityHomeBinding
import ua.turskyi.travelling.decoration.SectionAverageGapItemDecoration
import ua.turskyi.travelling.extensions.*
import ua.turskyi.travelling.features.allcountries.view.ui.AllCountriesActivity
import ua.turskyi.travelling.features.flags.view.FlagsActivity
import ua.turskyi.travelling.features.flags.view.FlagsActivity.Companion.POSITION
import ua.turskyi.travelling.features.home.view.adapter.HomeAdapter
import ua.turskyi.travelling.features.home.viewmodels.HomeActivityViewModel
import ua.turskyi.travelling.models.City
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.models.VisitedCountry
import ua.turskyi.travelling.utils.PermissionHandler
import ua.turskyi.travelling.utils.PermissionHandler.isPermissionGranted
import java.util.*
import kotlin.coroutines.CoroutineContext

class HomeActivity : AppCompatActivity(), CoroutineScope, DialogInterface.OnDismissListener,
    PurchasesUpdatedListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var billingClient: BillingClient
    private var backPressedTiming: Long = 0
    private var mLastClickTime: Long = 0
    private val homeViewModel by inject<HomeActivityViewModel>()
    private val homeAdapter by inject<HomeAdapter>()
    private val mSkuDetailsMap: MutableMap<String, SkuDetails> = HashMap()
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        PermissionHandler.checkPermission(this)
        initBilling()
        initListeners()
    }

    override fun onResume() {
        super.onResume()
        /* makes info icon visible */
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        /* makes sync icon visible */
        binding.toolbar.inflateMenu(R.menu.menu_sync)
        /*-----------*/
        launch { homeViewModel.initListOfCountries() }

        binding.circlePieChart.animatePieChart()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (!prefs.isSynchronized) {
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
                openInfoDialog()
                true
            }
            R.id.action_sync -> {
                if (prefs.isUpgraded) {
                    homeViewModel.syncDatabaseWithFireStore()
                } else {
                    val infoDialog =
                        InfoDialog.newInstance(getString(R.string.txt_info_billing), true)
                    infoDialog.show(supportFragmentManager, "billing dialog")
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

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            /*     we will get here after the purchase is made */
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            toast(R.string.toast_error_due_to_canceling_purchase)
        } else {
            toast(R.string.toast_error_unexpected)
        }
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
                    requestPermission()
                }
            }
        }
    }

    fun launchBilling(skuId: String) {
        val billingFlowParams = mSkuDetailsMap[skuId]?.let {
            BillingFlowParams.newBuilder()
                .setSkuDetails(it)
                .build()
        }
        billingFlowParams?.let { billingClient.launchBillingFlow(this, it) }
    }

    private fun requestPermission() = ActivityCompat.requestPermissions(
        this,
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).toTypedArray(),
        ACCESS_LOCATION_AND_EXTERNAL_STORAGE
    )

    fun initView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.viewModel = this.homeViewModel
        binding.lifecycleOwner = this
        setSupportActionBar(binding.toolbar)
        /* set drawable icon */
        supportActionBar?.setHomeAsUpIndicator(R.drawable.btn_info_ripple)

        val linearLayoutManager = LinearLayoutManager(this)
        binding.rvVisitedCountries.apply {
            this.adapter = homeAdapter
            this.layoutManager = linearLayoutManager
            this.addItemDecoration(
                SectionAverageGapItemDecoration(
                    10, 10, 20, 15
                )
            )
        }
        initGravityForTitle()
    }

    private fun initListeners() {
        homeAdapter.onFlagClickListener = {
            /* mis-clicking prevention, using threshold of 1000 ms */
            if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
                val intent = Intent(this@HomeActivity, FlagsActivity::class.java)
                val bundle = Bundle()
                bundle.putInt(POSITION, homeAdapter.getItemPosition(it))
                intent.putExtras(bundle)
                startActivity(intent)
            }
            mLastClickTime = SystemClock.elapsedRealtime()
        }
        homeAdapter.onLongClickListener = { countryNode ->
            val country = countryNode.mapNodeToActual()

            binding.root.showSnackWithAction(getString(R.string.delete_it, country.name)){
                action(R.string.yes){
                    homeViewModel.removeFromVisited(country)
                    longToast(getString(R.string.deleted, country.name))
                }
            }
        }

        homeAdapter.onCountryNameClickListener = { countryNode ->
            /* Creating the new Fragment with the Country id passed in. */
            val fragment = AddCityDialogFragment.newInstance(countryNode.id)
            fragment.show(supportFragmentManager, null)
        }
        homeAdapter.onCityLongClickListener = { city ->
           binding.root.showSnackWithAction(getString(R.string.delete_it, city.name)){
               action(R.string.yes) {
                   removeCityOnLongClick(city)
                   longToast(getString(R.string.deleted, city.name))
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

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            /*  Grant the item to the user */
            setUpgradedVersion()
            /* acknowledge the purchase */
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                launch {
                    withContext(Dispatchers.IO) {
                        val acknowledgePurchaseResponseListener =
                            AcknowledgePurchaseResponseListener { toast(R.string.toast_purchase_acknowledged) }
                        billingClient.acknowledgePurchase(
                            acknowledgePurchaseParams.build(),
                            acknowledgePurchaseResponseListener
                        )
                    }
                }
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            /*      Here we can confirm to the user that they've started the pending
                  purchase, and to complete it, they should follow instructions that
                  are given to them. You can also choose to remind the user in the
                  future to complete the purchase if you detect that it is still
                  pending. */
            toastLong(R.string.toast_complete_purchase)
        }
    }

    private fun initBilling() {
        billingClient =
            BillingClient.newBuilder(this).enablePendingPurchases().setListener(this)
                .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    /* The BillingClient is ready. Query purchases here. */
                    /* here we can request information about purchases */

                    /* Sku request */
                    querySkuDetails()

                    /* purchase request */
                    val purchasesList = queryPurchases()
                    /* if the product has already been purchased, provide it to the user */
                    purchasesList?.size?.let {
                        for (i in 0 until it) {
                            val purchaseId = purchasesList[i]?.sku
                            if (TextUtils.equals(SKU_ID, purchaseId)) {
                                setUpgradedVersion()
                            }
                        }
                    }
                } else {
                    toast(R.string.toast_connection_billing)
                }
            }

            override fun onBillingServiceDisconnected() {
                /* we get here if something goes wrong */
                /*   Try to restart the connection on the next request to
                    Google Play by calling the startConnection() method.*/
                toast(R.string.toast_internet_connection_lost)
            }

            private fun queryPurchases(): List<Purchase?>? {
                val purchasesResult: Purchase.PurchasesResult =
                    billingClient.queryPurchases(BillingClient.SkuType.INAPP)
                return purchasesResult.purchasesList
            }
        })
    }

    private fun setUpgradedVersion() {
        if (!prefs.isSynchronized) {
            homeViewModel.syncDatabaseWithFireStore()
        }
        prefs.isUpgraded = true
    }

    private fun querySkuDetails() {
        val skuDetailsParamsBuilder = SkuDetailsParams.newBuilder()
        val skuList: MutableList<String> = ArrayList()
        /* here we are adding the product id from the Play Console */
        skuList.add(SKU_ID)
        skuDetailsParamsBuilder.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        billingClient.querySkuDetailsAsync(skuDetailsParamsBuilder.build()) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (skuDetails in purchases) {
                    mSkuDetailsMap[skuDetails.sku] = skuDetails
                }
            }
        }
    }

    private fun openInfoDialog() = openInfoDialog(getString(R.string.txt_info_home))

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
        homeViewModel.visitedCountriesWithCities.observe(this, {
            if (homeViewModel.citiesCount > it.size) {
                binding.toolbarLayout.title = "${
                    resources.getQuantityString(
                        R.plurals.numberOfCitiesVisited,
                        homeViewModel.citiesCount,
                        homeViewModel.citiesCount
                    )
                } ${
                    resources.getQuantityString(
                        R.plurals.numberOfCountriesOfCitiesVisited, it.size,
                        it.size
                    )
                }"
            } else {
                binding.toolbarLayout.title = "${
                    resources.getQuantityString(
                        R.plurals.numberOfCitiesVisited,
                        it.size,
                        it.size
                    )
                } ${
                    resources.getQuantityString(
                        R.plurals.numberOfCountriesOfCitiesVisited, it.size,
                        it.size
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