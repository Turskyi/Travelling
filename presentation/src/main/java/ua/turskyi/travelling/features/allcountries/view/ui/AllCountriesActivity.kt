package ua.turskyi.travelling.features.allcountries.view.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_all_countries.*
import org.koin.android.ext.android.inject
import ua.turskyi.travelling.R
import ua.turskyi.travelling.features.allcountries.view.adapter.AllCountriesAdapter
import ua.turskyi.travelling.features.allcountries.viewmodel.AllCountriesActivityViewModel
import ua.turskyi.travelling.model.Country

class AllCountriesActivity : AppCompatActivity(R.layout.activity_all_countries) {

    private val viewModel: AllCountriesActivityViewModel by  inject()

    private val adapter: AllCountriesAdapter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListeners()
        initObservers()
    }

    private fun initView() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorBlack)
        adapter.submitList(viewModel.pagedList)
        val layoutManager = LinearLayoutManager(this)
        rvAllCountries.adapter = adapter
        rvAllCountries.layoutManager = layoutManager
    }

    private fun initListeners() {
        toolbar.setNavigationOnClickListener { onBackPressed() }
        adapter.onCountryClickListener = ::addToVisited
    }

    private fun addToVisited(country: Country) {
        viewModel.markAsVisited(country)
        onBackPressed()
    }

    private fun initObservers() {
        viewModel.notVisitedCountriesLiveData.observe(this, Observer { notVisitedNum ->
            updateTitle(notVisitedNum)
        })
        viewModel.visibilityLoader.observe(this, Observer { currentVisibility ->
            pb.visibility = currentVisibility
        })
    }

    private fun updateTitle(num: Int) {
        toolbarTitle.text = resources.getQuantityString(R.plurals.numberOfCountriesRemain, num, num)
    }
}