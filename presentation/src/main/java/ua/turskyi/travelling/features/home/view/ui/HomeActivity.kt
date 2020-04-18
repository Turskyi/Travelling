package ua.turskyi.travelling.features.home.view.ui

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import ua.turskyi.travelling.decoration.GridSectionAverageGapItemDecoration
import ua.turskyi.travelling.extensions.config
import ua.turskyi.travelling.extensions.mapNodeToActual
import ua.turskyi.travelling.extensions.spToPix
import ua.turskyi.travelling.features.allcountries.view.ui.AllCountriesActivity
import ua.turskyi.travelling.features.flags.view.FlagsActivity
import ua.turskyi.travelling.features.flags.view.FlagsActivity.Companion.POSITION
import ua.turskyi.travelling.features.home.view.adapter.HomeAdapter
import ua.turskyi.travelling.features.home.viewmodels.HomeActivityViewModel
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.models.CountryNode
import ua.turskyi.travelling.utils.IntFormatter
import kotlin.coroutines.CoroutineContext

class HomeActivity : AppCompatActivity(), CoroutineScope, DialogInterface.OnDismissListener  {

    companion object {
        const val ACCESS_LOCATION = 10001
        const val LOG_UPDATE = "LOG_UPDATE"
    }

    private val viewModel by inject<HomeActivityViewModel>()
    private val adapter by inject<HomeAdapter>()
    private lateinit var binding: ActivityHomeBinding

    private var mSnackBar: Snackbar? = null
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        checkPermission()
        initListeners()
    }

    override fun onResume() {
        super.onResume()
        launch {
            Log.d(LOG_UPDATE, "on Resume")
            viewModel.initList()
        }
    }

    private fun initView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this
        setSupportActionBar(toolbar)
        val layoutManager = LinearLayoutManager(this)
        rvVisitedCountries.adapter = this.adapter
        rvVisitedCountries.layoutManager = layoutManager
        rvVisitedCountries.addItemDecoration(
            GridSectionAverageGapItemDecoration(
                10, 10, 20, 15
            )
        )
    }

    private fun initListeners() {
        adapter.onImageClickListener = {
                val intent = Intent(this@HomeActivity, FlagsActivity::class.java)
                val bundle = Bundle()
                bundle.putInt(POSITION, adapter.itemCount - 1)
                intent.putExtras(bundle)
                startActivity(intent)
            }
        adapter.onLongClickListener = { countryNode ->
            showSnackBar(countryNode.mapNodeToActual())
        }

        adapter.onTextClickListener = { countryNode ->
            AddCityDialogFragment(countryNode).show(supportFragmentManager, null)
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

    private fun removeOnLongClick(country: Country) {
        viewModel.removeFromVisited(country)
    }

    private fun showSnackBar(country: Country) {
        mSnackBar = Snackbar.make(
            rvVisitedCountries,
            getString(R.string.delete_country, country.name),
            Snackbar.LENGTH_LONG
        ).setActionTextColor(Color.WHITE).setAction(getString(R.string.yes)) {
            removeOnLongClick(country)
            longToast(getString(R.string.deleted, country.name))
        }
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
                R.color.colorPrimaryDark
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

        /* remove hole inside */
        pieChart.isDrawHoleEnabled = false

        /* removes color squares */
        pieChart.legend.isEnabled = false

        /* remove default text "no chart data available */
        pieChart.setNoDataText(null)

        /* rotate the pie chart to 45 degrees */
        pieChart.rotationAngle = -10f

        /* updates data in pieChart */
        pieChart.invalidate()
    }

    private fun updateAdapter(countries: List<CountryNode>) {
        for (countryNode in countries) {
            countryNode.isExpanded = false
        }
        adapter.setList(countries)
        toolbarLayout.title = resources.getQuantityString(
            R.plurals.numberOfCountriesVisited,
            countries.size,
            countries.size
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onDismiss(p0: DialogInterface?) {
        Log.d(LOG_UPDATE, "on dismiss")
        launch {
            viewModel.initList()
        }
    }
}