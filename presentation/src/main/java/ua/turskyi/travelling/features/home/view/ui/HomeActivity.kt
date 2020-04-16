package ua.turskyi.travelling.features.home.view.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.entity.node.BaseNode
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
import ua.turskyi.travelling.extensions.mapActualListToBaseNodeList
import ua.turskyi.travelling.extensions.mapNodeToActual
import ua.turskyi.travelling.extensions.spToPix
import ua.turskyi.travelling.features.allcountries.view.ui.AllCountriesActivity
import ua.turskyi.travelling.features.flags.view.FlagsActivity
import ua.turskyi.travelling.features.flags.view.FlagsActivity.Companion.POSITION
import ua.turskyi.travelling.features.home.view.adapter.HomeAdapter
import ua.turskyi.travelling.features.home.viewmodel.HomeActivityViewModel
import ua.turskyi.travelling.models.CityNode
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.utils.IntFormatter
import java.util.ArrayList
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
        rvVisitedCountries.addItemDecoration(GridSectionAverageGapItemDecoration(10, 10, 20, 15))
    }

    private fun initListeners() {
        adapter.onImageClickListener = {
                val intent = Intent(this@HomeActivity, FlagsActivity::class.java)
                val bundle = Bundle()
                bundle.putInt(POSITION, adapter.itemCount - 1)
                intent.putExtras(bundle)
                startActivity(intent)
            }
        adapter.onLongClickListener = {
            it?.mapNodeToActual()?.let { it1 -> showSnackBar(it1) }
        }

        adapter.onTextClickListener = {
//            TODO: implement dialogue fragment with edit text and geo location
//             with opportunity to add a city
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
        viewModel.visitedCountries.observe(
            this,
            Observer { visitedCountries ->
                initPieChart(visitedCountries)
                updateAdapter(visitedCountries)
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

    private fun updateAdapter(countries: List<Country>) {
        val countryNodeList = countries.mapActualListToBaseNodeList()
        for (country in countryNodeList){
            /* Item Node*/
            val itemEntity1 = CityNode("Root ${country.title} - city 0")
            val itemEntity2 = CityNode("Root ${country.title} - city 1")
            val itemEntity3 = CityNode("Root ${country.title} - city 2")
            val itemEntity4 = CityNode("Root ${country.title} - city 3")
            val itemEntity5 = CityNode("Root ${country.title} - city 4")
            val items: MutableList<BaseNode> = ArrayList()
            items.add(itemEntity1)
            items.add(itemEntity2)
            items.add(itemEntity3)
            items.add(itemEntity4)
            items.add(itemEntity5)
            country.childNode = items
            country.isExpanded = false
        }
        adapter.setList(countryNodeList)
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