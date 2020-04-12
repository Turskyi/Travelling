package ua.turskyi.travelling.features.allcountries.view.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_all_countries.*
import kotlinx.android.synthetic.main.activity_all_countries.toolbar
import org.koin.android.ext.android.inject
import ua.turskyi.travelling.R
import ua.turskyi.travelling.model.Country
import ua.turskyi.travelling.features.allcountries.view.adapter.AllCountriesAdapter
import ua.turskyi.travelling.features.allcountries.viewmodel.AllCountriesActivityViewModel

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
        viewModel.pagedList.observe(this, Observer {
            adapter.submitList(it)
        })
        viewModel.countriesLiveData.observe(this, Observer { offlineCountries ->
            updateTitle(offlineCountries)
        })
        viewModel.visibilityLoader.observe(this, Observer { currentVisibility ->
            pb.visibility = currentVisibility
        })
    }

    private fun updateTitle(countries: List<Country>) {
        toolbarTitle.text = getString(R.string.num_of_countries, countries.size.toString())
    }
}