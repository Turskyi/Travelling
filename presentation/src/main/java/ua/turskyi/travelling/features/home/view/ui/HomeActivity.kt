package ua.turskyi.travelling.features.home.view.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
import ua.turskyi.travelling.extensions.config
import ua.turskyi.travelling.extensions.spToPix
import ua.turskyi.travelling.features.allcountries.view.ui.AllCountriesActivity
import ua.turskyi.travelling.features.home.view.adapter.HomeAdapter
import ua.turskyi.travelling.features.home.view.callback.OnVisitedCountryClickListener
import ua.turskyi.travelling.features.home.viewmodel.HomeActivityViewModel
import ua.turskyi.travelling.features.selfie.view.SelfieActivity
import ua.turskyi.travelling.model.Country
import ua.turskyi.travelling.utils.IntFormatter
import kotlin.coroutines.CoroutineContext

class HomeActivity : AppCompatActivity(), CoroutineScope {

    private val viewModel by inject<HomeActivityViewModel>()
    private lateinit var binding: ActivityHomeBinding

    private val adapter by inject<HomeAdapter>()
    private var mSnackBar: Snackbar? = null
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        initView()
        initListeners()
        initObservers()
    }

    override fun onResume() {
        super.onResume()
        launch {
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
    }

    private fun initListeners() {
        adapter.setOnItemClickListener(object :
            OnVisitedCountryClickListener {
            override fun onItemClick(country: Country) = start<SelfieActivity>()
            override fun onItemLongClick(country: Country) = showSnackBar(country)

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

            private fun removeOnLongClick(country: Country) {
                viewModel.removeFromVisited(country)
            }
        })
    }

    private fun initObservers() {
        viewModel.navigateToAllCountries.observe(this,
            Observer { shouldNavigate ->
                if (shouldNavigate == true) {
                    start<AllCountriesActivity>()
                    viewModel.onNavigatedToAllCountries()
                }
            })
        viewModel.visitedCountries.observe(
            this,
            Observer { visitedCountries ->
                initPieChart(visitedCountries)
                updateAdapter(visitedCountries)
                showFloatBtn(visitedCountries)
            })
        adapter.visibilityLoader.observe(this, Observer { currentVisibility ->
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

    private fun updateAdapter(countries: List<Country>) {
        adapter.setData(countries)
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
}