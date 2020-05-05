package ua.turskyi.travelling.features.home.view.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
import splitties.toast.toast
import ua.turskyi.travelling.Constant.GOOGLE_PLAY_ADDRESS
import ua.turskyi.travelling.R
import ua.turskyi.travelling.databinding.ActivityHomeBinding
import ua.turskyi.travelling.decoration.SectionAverageGapItemDecoration
import ua.turskyi.travelling.extensions.config
import ua.turskyi.travelling.extensions.mapNodeToActual
import ua.turskyi.travelling.extensions.mapViewToBitmap
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
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

/* # milliseconds, desired time passed between two back presses. */
private const val TIME_INTERVAL = 2000

class HomeActivity : AppCompatActivity(), CoroutineScope, DialogInterface.OnDismissListener,
    OnChartGestureListener {

    companion object {
        const val ACCESS_LOCATION = 10001
        const val LOG_UPDATE = "LOG_UPDATE"
    }

    private lateinit var binding: ActivityHomeBinding
    private var backPressedTiming: Long = 0
    private var mSnackBar: Snackbar? = null
    private var job: Job = Job()
    private val viewModel by inject<HomeActivityViewModel>()
    private val adapter by inject<HomeAdapter>()
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
        val bitmap = getScreenShot(toolbarLayout)
        val fileName = "piechart${Random.nextInt(0, 1000)}.jpg"
        val file = bitmap?.let { storeFileAs(it, fileName) }
        file?.let { shareImage(file = it) }
    }
    override fun onChartDoubleTapped(me: MotionEvent?) {}
    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {}
    override fun onChartSingleTapped(me: MotionEvent?) {
        when (pieChart.isDrawHoleEnabled) {
            false -> {
                pieChart.isDrawHoleEnabled = true
                pieChart.centerText = setCenterPictureViaSpannableString()
            }
            true -> {
                pieChart.centerText = ""
                pieChart.isDrawHoleEnabled = false
            }
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResult: IntArray
    ) {
        when (requestCode) {
            ACCESS_LOCATION -> {
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

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun shareImage(file: File) {
        val uri = FileProvider.getUriForFile(
            this,
            applicationContext.packageName.toString() + ".provider",
            file
        )
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/*"
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name)
        intent.putExtra(Intent.EXTRA_TEXT, GOOGLE_PLAY_ADDRESS)
        intent.putExtra(Intent.EXTRA_STREAM, uri)

        try {
            startActivity(
                Intent.createChooser(
                    intent,
                    "Share How Many Countries You Have Visited With Your Friends"
                )
            )
        } catch (e: ActivityNotFoundException) {
            toast("No App Available")
        }
    }

    private fun initView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this
        setSupportActionBar(toolbar)

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

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width: Int = displayMetrics.widthPixels
        Log.d(LOG_UPDATE, "width: ${width}")
        if (width < 1082){
            toolbarLayout.expandedTitleGravity = Gravity.BOTTOM
        }
    }

    private fun storeFileAs(bitmap: Bitmap, fileName: String): File {
        val dirPath =
            externalCacheDir?.absolutePath + "/Screenshots"
        val dir = File(dirPath)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dirPath, fileName)
        try {
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file
    }

    private fun getScreenShot(view: View): Bitmap? {
        return view.mapViewToBitmap()?.let { Bitmap.createBitmap(it) }
    }
    private fun initListeners() {
        pieChart.onChartGestureListener = this
        adapter.onImageClickListener = {
                val intent = Intent(this@HomeActivity, FlagsActivity::class.java)
                val bundle = Bundle()
                bundle.putInt(POSITION, adapter.getItemPosition(it))
                intent.putExtras(bundle)
                startActivity(intent)
            }
        adapter.onLongClickListener = { countryNode ->
            showSnackBarWithCountry(countryNode.mapNodeToActual())
        }

        adapter.onCountryNameClickListener = { countryNode ->
            AddCityDialogFragment(countryNode).show(supportFragmentManager, null)
        }
        adapter.onCityLongClickListener = { city ->
            showSnackBarWithThis(city)
        }
    }

    private fun checkPermission() {
        val permissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permissionGranted != PackageManager.PERMISSION_GRANTED) {
            requestPermission()
        } else {
            initView()
            initObservers()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            listOf(Manifest.permission.ACCESS_FINE_LOCATION).toTypedArray(),
            ACCESS_LOCATION
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
        )
            .setActionTextColor(Color.WHITE)
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
            updateAdapter(it)
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

        /* remove or enable hole inside */
        pieChart.isDrawHoleEnabled = false

        pieChart.holeRadius = 80F

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

    private fun updateAdapter(visitedCountries: List<VisitedCountry>) {
        for (countryNode in visitedCountries) {
            countryNode.isExpanded = false
        }
        adapter.setList(visitedCountries)
        toolbarLayout.title = resources.getQuantityString(
            R.plurals.numberOfCountriesVisited,
            visitedCountries.size,
            visitedCountries.size
        )
    }
}