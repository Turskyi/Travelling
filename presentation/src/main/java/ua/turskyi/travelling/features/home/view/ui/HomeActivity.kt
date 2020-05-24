package ua.turskyi.travelling.features.home.view.ui

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_home.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import splitties.activities.start
import splitties.toast.longToast
import ua.turskyi.travelling.R
import ua.turskyi.travelling.databinding.ActivityHomeBinding
import ua.turskyi.travelling.decoration.SectionAverageGapItemDecoration
import ua.turskyi.travelling.extensions.config
import ua.turskyi.travelling.extensions.mapNodeToActual
import ua.turskyi.travelling.extensions.spToPix
import ua.turskyi.travelling.features.allcountries.view.ui.AllCountriesActivity
import ua.turskyi.travelling.features.flags.view.FlagsActivity
import ua.turskyi.travelling.features.flags.view.FlagsActivity.Companion.POSITION
import ua.turskyi.travelling.features.home.view.adapter.HomeAdapter
import ua.turskyi.travelling.features.home.viewmodels.HomeActivityViewModel
import ua.turskyi.travelling.models.City
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.models.VisitedCountry
import ua.turskyi.travelling.utils.IntFormatter
import kotlin.coroutines.CoroutineContext

/* # milliseconds, desired time passed between two back presses. */
private const val TIME_INTERVAL = 2000
class HomeActivity : AppCompatActivity(), CoroutineScope, DialogInterface.OnDismissListener,
    OnChartGestureListener {

    companion object {
        const val ACCESS_LOCATION_AND_EXTERNAL_STORAGE = 10001
    }

    private lateinit var binding: ActivityHomeBinding
    private var backPressedTiming: Long = 0
    private var mSnackBar: Snackbar? = null
    private var mLastClickTime: Long = 0
    private val viewModel by inject<HomeActivityViewModel>()
    private val adapter by inject<HomeAdapter>()
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        checkPermission()
        initListeners()
    }

    override fun onChartGestureEnd(
        me: MotionEvent?,
        lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) {
    }

    override fun onChartFling(
        me1: MotionEvent?,
        me2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ) {
    }

    override fun onChartGestureStart(
        me: MotionEvent?,
        lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) {
    }

    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {}
    override fun onChartLongPressed(me: MotionEvent?) {
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        val bottomSheet = ShareListBottomSheetDialog()
        bottomSheet.show(supportFragmentManager, null)
    }
    override fun onChartDoubleTapped(me: MotionEvent?) {}
    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {}
    override fun onChartSingleTapped(me: MotionEvent?) {
        when (pieChart.isDrawHoleEnabled) {
            false -> {
                pieChart.isDrawHoleEnabled = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    pieChart.centerText = setCenterPictureViaSpannableString()
                }
                showTitleWithOnlyCountries()
            }
            true -> {
                pieChart.centerText = ""
                pieChart.isDrawHoleEnabled = false
                if (viewModel.citiesCount > 0) {
                    showTitleWithCitiesAndCountries()
                } else {
                    showTitleWithOnlyCountries()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResult: IntArray
    ) {
        when (requestCode) {
            ACCESS_LOCATION_AND_EXTERNAL_STORAGE -> {
                if ((grantResult.isNotEmpty() && grantResult[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    initView()
                    initObservers()
                } else {
                    requestPermission()
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        launch {
            viewModel.initList()
        }
    }

    override fun onDismiss(p0: DialogInterface?) {
        launch {
            viewModel.initList()
        }
    }

    override fun onBackPressed() {
        if (backPressedTiming + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed()
            return
        } else {
            Snackbar.make(
                rvVisitedCountries,
                resources.getString(R.string.tap_back_button_in_order_to_exit),
                Snackbar.LENGTH_SHORT
            ).show()
        }
        backPressedTiming = System.currentTimeMillis()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                openInfoDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun openInfoDialog() {
        val infoDialog = InfoDialog.newInstance(getString(R.string.txt_info_home))
        infoDialog.show(supportFragmentManager, "info dialog")
    }

    private fun initView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this
        setSupportActionBar(toolbar)
        /* set drawable icon */
        supportActionBar?.setHomeAsUpIndicator(R.drawable.btn_info_ripple)

        /* remove default text "no chart data available */
        pieChart.setNoDataText(null)

        val layoutManager = LinearLayoutManager(this)
        rvVisitedCountries.adapter = this.adapter
        rvVisitedCountries.layoutManager = layoutManager
        rvVisitedCountries.addItemDecoration(
            SectionAverageGapItemDecoration(
                10, 10, 20, 15
            )
        )

        initGravityForTitle()
    }

    private fun initGravityForTitle() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width: Int = displayMetrics.widthPixels
        if (width < 1082) {
            toolbarLayout.expandedTitleGravity = Gravity.BOTTOM
        }
    }

    private fun initListeners() {
        adapter.onImageClickListener = {
            /* mis-clicking prevention, using threshold of 1000 ms */
            if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
                val intent = Intent(this@HomeActivity, FlagsActivity::class.java)
                val bundle = Bundle()
                bundle.putInt(POSITION, adapter.getItemPosition(it))
                intent.putExtras(bundle)
                startActivity(intent)
            }
            mLastClickTime = SystemClock.elapsedRealtime()
        }
        adapter.onLongClickListener = { countryNode ->
            showSnackBarWithCountry(countryNode.mapNodeToActual())
        }

        adapter.onCountryNameClickListener = { countryNode ->
            /* Creating the new Fragment with the Country id passed in. */
            val fragment = AddCityDialogFragment.newInstance(countryNode.id)
            fragment.show(supportFragmentManager, null)
        }
        adapter.onCityLongClickListener = { city ->
            showSnackBarWithThis(city)
        }
    }

    private fun checkPermission() {
        val locationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val externalStoragePermission =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        if (locationPermission != PackageManager.PERMISSION_GRANTED && externalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            requestPermission()
        } else {
            initView()
            initObservers()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE).toTypedArray(),
            ACCESS_LOCATION_AND_EXTERNAL_STORAGE
        )
    }

    private fun removeCountryOnLongClick(country: Country) {
        viewModel.removeFromVisited(country)
    }

    private fun removeCityOnLongClick(city: City) {
       viewModel.removeCity(city)
    }

    private fun showSnackBarWithThis(city: City) {
        mSnackBar = Snackbar.make(
            rvVisitedCountries,
            getString(R.string.delete_it, city.name),
            Snackbar.LENGTH_LONG
        ).setActionTextColor(Color.WHITE)
            .setAction(getString(R.string.yes)) {
                removeCityOnLongClick(city)
                longToast(getString(R.string.deleted, city.name))
            }
        decorateSnackbar()
    }

    private fun showSnackBarWithCountry(country: Country) {
        mSnackBar = Snackbar.make(
            rvVisitedCountries,
            getString(R.string.delete_it, country.name),
            Snackbar.LENGTH_LONG
        ).setActionTextColor(Color.WHITE).setAction(getString(R.string.yes)) {
            removeCountryOnLongClick(country)
            longToast(getString(R.string.deleted, country.name))
        }
        decorateSnackbar()
    }

    private fun decorateSnackbar() {
        mSnackBar?.config(applicationContext)
        mSnackBar?.show()

        val snackView = mSnackBar?.view
        val snackTextView =
            snackView?.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        snackTextView?.setTextColor(Color.RED)
    }

    private fun initObservers() {
        viewModel.navigateToAllCountries.observe(this,
            Observer { shouldNavigate ->
                if (shouldNavigate == true) {
                    start<AllCountriesActivity>()
                    viewModel.onNavigatedToAllCountries()
                }
            })
        viewModel.visitedCountriesWithCities.observe(this, Observer {
            updateAdapterWith(it)
            initTitleWithNumberOf(it)
        })
        viewModel.visitedCountries.observe(
            this,
            Observer { visitedCountries ->
                initPieChart(visitedCountries)
                showFloatBtn(visitedCountries)
            })
        viewModel.visibilityLoader.observe(this, Observer { currentVisibility ->
            pb.visibility = currentVisibility
        })
    }

    private fun showFloatBtn(visitedCountries: List<Country>?) {
        if (visitedCountries.isNullOrEmpty()) {
            floatBtnLarge.show()
            floatBtnSmall.visibility = View.GONE
        } else {
            floatBtnLarge.hide()
            floatBtnSmall.show()
        }
    }

    private fun initPieChart(visitedCountries: List<Country>) {
        val entries: MutableList<PieEntry> = mutableListOf()
        entries.add(PieEntry(visitedCountries.size.toFloat()))
        entries.add(PieEntry(viewModel.notVisitedCount.toFloat()))
        val pieChartColors: MutableList<Int> = mutableListOf()
        pieChartColors.add(ContextCompat.getColor(applicationContext, R.color.colorAccent))
        pieChartColors.add(
            ContextCompat.getColor(
                applicationContext,
                R.color.colorBrightBlue
            )
        )

        val dataSet = PieDataSet(entries, null)
        dataSet.colors = pieChartColors

        val data = PieData(dataSet)
        data.setValueFormatter(IntFormatter())
        data.setValueTextSize(applicationContext.spToPix(R.dimen.caption))
        data.setValueTextColor(Color.WHITE)

        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.onChartGestureListener = this

        /* remove or enable hole inside */
        pieChart.isDrawHoleEnabled = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pieChart.holeRadius = 80F
        } else {
            pieChart.holeRadius = 20F
            pieChart.setTransparentCircleColor(Color.BLACK)
            pieChart.transparentCircleRadius = 24F
            pieChart.setHoleColor(Color.BLACK)
        }

        /* removes color squares */
        pieChart.legend.isEnabled = false

        /* rotate the pie chart to 45 degrees */
        pieChart.rotationAngle = -10f

        pieChart.setBackgroundResource(R.drawable.gradient_list)
        val animationDrawable: AnimationDrawable =
            pieChart.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(2000)
        animationDrawable.setExitFadeDuration(4000)
        animationDrawable.start()

        /* updates data in pieChart every time */
        pieChart.invalidate()
    }

    private fun setCenterPictureViaSpannableString(): SpannableString? {
        val imageSpan = ImageSpan(this, R.drawable.pic_pie_chart_center)
        val spannableString = SpannableString(" ")
        spannableString.setSpan(imageSpan, " ".length - 1, " ".length, 0)
        return spannableString
    }

    private fun updateAdapterWith(visitedCountries: List<VisitedCountry>) {
        for (countryNode in visitedCountries) {
            countryNode.isExpanded = false
        }
        adapter.setList(visitedCountries)
    }

    private fun initTitleWithNumberOf(visitedCountries: List<VisitedCountry>) {
        if (viewModel.citiesCount == 0) {
            toolbarLayout.title = resources.getQuantityString(
                R.plurals.numberOfCountriesVisited,
                visitedCountries.size,
                visitedCountries.size
            )
        } else {
            if (viewModel.citiesCount > visitedCountries.size) {
                toolbarLayout.title = "${resources.getQuantityString(
                    R.plurals.numberOfCitiesVisited,
                    viewModel.citiesCount,
                    viewModel.citiesCount
                )} ${resources.getQuantityString(
                    R.plurals.numberOfCountriesOfCitiesVisited, visitedCountries.size,
                    visitedCountries.size
                )}"
            } else {
                toolbarLayout.title = "${resources.getQuantityString(
                    R.plurals.numberOfCitiesVisited,
                    visitedCountries.size,
                    visitedCountries.size
                )} ${resources.getQuantityString(
                    R.plurals.numberOfCountriesOfCitiesVisited, visitedCountries.size,
                    visitedCountries.size
                )}"
            }
        }
    }

    private fun showTitleWithCitiesAndCountries() {
        viewModel.visitedCountriesWithCities.observe(this, Observer {
            if (viewModel.citiesCount > it.size) {
                toolbarLayout.title = "${resources.getQuantityString(
                    R.plurals.numberOfCitiesVisited,
                    viewModel.citiesCount,
                    viewModel.citiesCount
                )} ${resources.getQuantityString(
                    R.plurals.numberOfCountriesOfCitiesVisited, it.size,
                    it.size
                )}"
            } else {
                toolbarLayout.title = "${resources.getQuantityString(
                    R.plurals.numberOfCitiesVisited,
                    it.size,
                    it.size
                )} ${resources.getQuantityString(
                    R.plurals.numberOfCountriesOfCitiesVisited, it.size,
                    it.size
                )}"
            }
        })
    }

    private fun showTitleWithOnlyCountries() {
        viewModel.visitedCountriesWithCities.observe(this, Observer {
            toolbarLayout.title = resources.getQuantityString(
                R.plurals.numberOfCountriesVisited,
                it.size,
                it.size
            )
        })
    }
}