package ua.turskyi.travelling.features.home.view.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
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
import ua.turskyi.travelling.utils.Event
import ua.turskyi.travelling.utils.extensions.action
import ua.turskyi.travelling.utils.extensions.getScreenWidth
import ua.turskyi.travelling.utils.extensions.mapNodeToActual
import ua.turskyi.travelling.utils.extensions.openActivityWithArgs
import ua.turskyi.travelling.utils.extensions.openInfoDialog
import ua.turskyi.travelling.utils.extensions.showSnackBar
import ua.turskyi.travelling.utils.extensions.showSnackWithAction
import ua.turskyi.travelling.utils.extensions.toast
import ua.turskyi.travelling.utils.extensions.toastLong
import ua.turskyi.travelling.utils.getDisplayName
import ua.turskyi.travelling.widgets.CirclePieChart
import ua.turskyi.travelling.widgets.VisitedCountriesWidgetProvider

class HomeActivity : AppCompatActivity(), DialogInterface.OnDismissListener,
    SyncDialog.SyncListener {
    private val viewModel: HomeActivityViewModel by inject()
    private val homeAdapter: HomeAdapter by inject()
    private val authInteractor: ua.turskyi.domain.interactor.AuthInteractor by inject()

    private lateinit var binding: ActivityHomeBinding
    private lateinit var allCountriesResultLauncher: ActivityResultLauncher<Intent>
    private var isTravelersFlow = false

    fun refreshData() {
        viewModel.showListOfVisitedCountries()
        invalidateOptionsMenu()
    }

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
        invalidateOptionsMenu()
        viewModel.showListOfVisitedCountries()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_sync, menu)
        updateAccountIcon(menu.findItem(R.id.action_account))
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        updateAccountIcon(menu.findItem(R.id.action_account))
        return super.onPrepareOptionsMenu(menu)
    }

    private fun updateAccountIcon(accountItem: MenuItem?) {
        if (accountItem == null) return
        val user = authInteractor.getCurrentUser()
        if (user != null && user.photoUrl.isNotEmpty()) {
            com.bumptech.glide.Glide.with(this)
                .asBitmap()
                .load(user.photoUrl)
                .circleCrop()
                .into(object :
                    com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>(
                        96,
                        96
                    ) {
                    override fun onResourceReady(
                        resource: android.graphics.Bitmap,
                        transition: com.bumptech.glide.request.transition.Transition<in android.graphics.Bitmap>?
                    ) {
                        accountItem.icon =
                            androidx.core.graphics.drawable.RoundedBitmapDrawableFactory.create(
                                resources,
                                resource
                            ).apply {
                                isCircular = true
                            }
                    }

                    override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
                })
        } else {
            accountItem.icon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_myplaces)
        }
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
    private var pendingKeepSignedIn = false

    override fun onInitiateGoogleSignIn(keepSignedIn: Boolean) {
        pendingKeepSignedIn = keepSignedIn
        lifecycleScope.launch {
            try {
                val credentialManager = androidx.credentials.CredentialManager.create(this@HomeActivity)
                val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .build()

                val request = androidx.credentials.GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(this@HomeActivity, request)
                val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(result.credential.data)
                val idToken = googleIdTokenCredential.idToken

                val authInteractor: ua.turskyi.domain.interactor.AuthInteractor by inject()
                val signInResult = authInteractor.signInWithGoogleCredential(idToken, pendingKeepSignedIn)
                signInResult.onSuccess { user ->
                    toast(getString(R.string.status_signed_in_as, user.email.ifEmpty { user.displayName }))
                    val travellerRepo: ua.turskyi.domain.repository.TravellerRepository by inject()
                    travellerRepo.saveTraveller({}, {})
                    refreshData()
                    val syncDialog = SyncDialog.newInstance(isTravelersFlow)
                    syncDialog.show(
                        supportFragmentManager,
                        resources.getString(R.string.tag_sync_dialog)
                    )
                }.onFailure { e ->
                    toast(getString(R.string.msg_sign_in_failed, e.localizedMessage ?: e.toString()))
                }
            } catch (e: Exception) {
                toast(getString(R.string.msg_google_signin_failed, e.localizedMessage ?: e.toString()))
            }
        }
    }

    override fun showTravellingPro() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    resources.getString(R.string.play_market_app_link_to_pro_version).toUri()
                )
            )
        } catch (e: ActivityNotFoundException) {
            toast(e.localizedMessage ?: e.stackTraceToString())
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    resources.getString(R.string.play_market_web_link_to_pro_version).toUri()
                )
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                openInfoDialog(R.string.txt_info_home)
                true
            }

            R.id.action_language -> {
                showLanguageDialog()
                true
            }

            R.id.action_account -> {
                startActivity(
                    Intent(
                        this,
                        ua.turskyi.travelling.features.account.view.AccountActivity::class.java
                    )
                )
                true
            }

            R.id.action_travellers -> {
                if (authInteractor.getCurrentUser() != null) {
                    startActivity(
                        Intent(
                            this,
                            ua.turskyi.travelling.features.travellers.view.TravellersActivity::class.java
                        )
                    )
                } else {
                    isTravelersFlow = true
                    showTravelersLoginDialog()
                }
                true
            }

            R.id.action_sync -> {
                isTravelersFlow = false
                val syncDialog: SyncDialog = SyncDialog.newInstance(false)
                syncDialog.show(
                    this.supportFragmentManager,
                    resources.getString(R.string.tag_sync_dialog),
                )
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf(
            getString(R.string.language_system_default),
            getString(R.string.language_en),
            getString(R.string.language_uk),
        )
        val currentLocales: LocaleListCompat = AppCompatDelegate.getApplicationLocales()
        val currentLanguageIndex = when {
            currentLocales.isEmpty -> 0
            currentLocales[0]?.language == "en" -> 1
            currentLocales[0]?.language == "uk" -> 2
            else -> 0
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.language_dialog_title)
            .setSingleChoiceItems(languages, currentLanguageIndex) { dialog, which ->
                val targetLocales = when (which) {
                    1 -> LocaleListCompat.forLanguageTags("en")
                    2 -> LocaleListCompat.forLanguageTags("uk")
                    else -> LocaleListCompat.getEmptyLocaleList()
                }
                AppCompatDelegate.setApplicationLocales(targetLocales)
                VisitedCountriesWidgetProvider.updateWidget(this)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.home_dialog_btn_cancel, null)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResult: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResult)
        when (requestCode) {
            resources.getInteger(R.integer.location_and_storage_request_code) -> {
                if ((grantResult.isNotEmpty() &&
                            grantResult.first() == PackageManager.PERMISSION_GRANTED)
                ) {
                    // we got here the first time, when permission is received
                    viewModel.isPermissionGranted = true
                    initObservers()
                } else {
                    requestPermission(this)
                }
            }
        }
    }

    private fun initView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this
        setSupportActionBar(binding.toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBar) { view, insets ->
            val cutoutInsets = insets.getInsets(
                WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.updatePadding(top = cutoutInsets.top)
            insets
        }
        @Suppress("DEPRECATION")
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorBlack)
        // set drawable icon
        if (supportActionBar != null) {
            supportActionBar?.setHomeAsUpIndicator(R.drawable.btn_info_ripple)
        }

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
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (viewModel.isDoubleBackToExitPressed) {
                        finish()
                    } else {
                        binding.root.showSnackBar(R.string.tap_back_button_in_order_to_exit)
                        viewModel.isDoubleBackToExitPressed = true
                        Handler(Looper.getMainLooper()).postDelayed(
                            { viewModel.isDoubleBackToExitPressed = false },
                            resources.getInteger(R.integer.desired_time_interval).toLong(),
                        )
                    }
                }
            },
        )
        homeAdapter.apply {
            onFlagClickListener = { country: VisitedCountry ->
                // mis-clicking prevention, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - viewModel.mLastClickTime > resources.getInteger(
                        R.integer.click_interval
                    )
                ) {
                    val position = viewModel.visitedCountries.value?.indexOfFirst { it.id == country.id }
                        ?.takeIf { it != -1 } ?: getItemPosition(country)
                    openActivityWithArgs(FlagsActivity::class.java) {
                        putInt(EXTRA_POSITION, position)
                        if (viewModel.visitedCountries.value != null) {
                            putInt(EXTRA_ITEM_COUNT, viewModel.visitedCountries.value!!.size)
                        }
                    }
                }
                viewModel.mLastClickTime = SystemClock.elapsedRealtime()
            }
            onLongClickListener = { countryNode: VisitedCountry ->
                val country: Country = countryNode.mapNodeToActual()
                val displayName: String = country.getDisplayName(this@HomeActivity)

                binding.root.showSnackWithAction(getString(R.string.delete_it, displayName)) {
                    action(R.string.yes) {
                        viewModel.removeFromVisited(country)
                        toastLong(getString(R.string.deleted, displayName))
                    }
                }
            }

            onCountryNameClickListener = { countryNode: VisitedCountry ->
                /* Showing the new Dialog Fragment with the Country id passed in,
                to add a new city to the country */
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
        viewModel.visitedCountriesWithCities.observe(this) { visitedCountries ->
            initTitleWithNumberOf(visitedCountries)
            updateAdapterWith(visitedCountries)
            VisitedCountriesWidgetProvider.updateWidget(this)
        }
        viewModel.visitedCountries.observe(this) { visitedCountries ->
            binding.circlePieChart.apply {
                initPieChart()
                createPieChartWith(visitedCountries, viewModel.notVisitedCountriesCount)
                binding.circlePieChart.animatePieChart()
            }
            showFloatBtn(visitedCountries)
        }
        viewModel.visibilityLoader.observe(this) { currentVisibility ->
            binding.pb.visibility = currentVisibility
        }

        viewModel.errorMessage.observe(this) { event: Event<String> ->
            val errorMessage: String? = event.getMessageIfNotHandled()
            if (errorMessage != null) {
                toastLong(errorMessage)
            }
        }
        /*  here could be a more efficient way to handle a click to open activity,
         * but it is made on purpose of demonstration databinding */
        viewModel.navigateToAllCountries.observe(this) { shouldNavigate: Boolean ->
            if (shouldNavigate) {
                allCountriesResultLauncher.launch(Intent(this, AllCountriesActivity::class.java))
                viewModel.onNavigatedToAllCountries()
            }
        }
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
                // did not add country to visited list
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
        for (countryNode: VisitedCountry in visitedCountries) {
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
        viewModel.visitedCountriesWithCities.observe(this) { countries ->
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
        }
    }

    /** [showTitleWithOnlyCountries] must be open to use it in custom "circle pie chart" widget */
    fun showTitleWithOnlyCountries() {
        viewModel.visitedCountriesWithCities.observe(
            this,
        ) { countryList: List<VisitedCountry> ->
            binding.toolbarLayout.title = resources.getQuantityString(
                R.plurals.numberOfCountriesVisited,
                countryList.size,
                countryList.size
            )
        }
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
        if (locationPermission != PackageManager.PERMISSION_GRANTED &&
            externalStoragePermission != PackageManager.PERMISSION_GRANTED
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
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ),
        resources.getInteger(R.integer.location_and_storage_request_code)
    )

    private fun showTravelersLoginDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_sync_explanation, null)
        val tvMessage = view.findViewById<android.widget.TextView>(R.id.tvExplanation)
        val cbKeepSignedIn = view.findViewById<android.widget.CheckBox>(R.id.cbKeepSignedIn)

        tvMessage.text = getString(R.string.travelers_sync_explanation)
        cbKeepSignedIn.isChecked = false

        AlertDialog.Builder(this)
            .setView(view)
            .setTitle(getString(R.string.travelers_dialog_title))
            .setPositiveButton(getString(R.string.btn_sign_in_google)) { _, _ ->
                authInteractor.setKeepSignedIn(cbKeepSignedIn.isChecked)
                onInitiateGoogleSignIn(cbKeepSignedIn.isChecked)
            }
            .setNegativeButton(getString(R.string.home_dialog_btn_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}