package ua.turskyi.travelling.features.allcountries.view.ui

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_all_countries.*
import org.koin.android.ext.android.inject
import ua.turskyi.travelling.R
import ua.turskyi.travelling.features.allcountries.view.adapter.AllCountriesAdapter
import ua.turskyi.travelling.features.allcountries.viewmodel.AllCountriesActivityViewModel
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.utils.hideKeyboard
import ua.turskyi.travelling.utils.showKeyboard

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
        ibSearch.setOnClickListener {
            if (it.isSelected) {
                collapseSearch()
            } else {
                expandSearch()
            }
        }
        etSearch.addTextChangedListener {
            viewModel.searchQuery = it.toString()
            adapter.submitList(viewModel.pagedList)
        }

        toolbar.setNavigationOnClickListener { onBackPressed() }
        adapter.onCountryClickListener = ::addToVisited
    }

    private fun addToVisited(country: Country) {
        viewModel.markAsVisited(country)
        onBackPressed()
    }

    private fun initObservers() {
        viewModel.notVisitedCountriesNumLiveData.observe(this, Observer { notVisitedNum ->
            updateTitle(notVisitedNum)
        })
        viewModel.visibilityLoader.observe(this, Observer { currentVisibility ->
            pb.visibility = currentVisibility
        })
    }

    private fun updateTitle(num: Int) {
        toolbarTitle.text = resources.getQuantityString(R.plurals.numberOfCountriesRemain, num, num)
    }

    private fun collapseSearch() {
        rvAllCountries.animate()
            .translationY((-1 * resources.getDimensionPixelSize(R.dimen.offset_20)).toFloat())
        ibSearch.isSelected = false
        val width =
            toolbarTitle.width - resources.getDimensionPixelSize(R.dimen.offset_16)
        hideKeyboard()
        etSearch.setText("")
        toolbarTitle.animate().alpha(1f).duration = 200
        sllSearch.elevate(
            resources.getDimension(R.dimen.elevation_8),
            resources.getDimension(R.dimen.elevation_1),
            100
        )
        ValueAnimator.ofInt(
            width,
            0
        ).apply {
            addUpdateListener {
                etSearch.layoutParams.width = animatedValue as Int
                sllSearch.requestLayout()
            }
            duration = 400
        }.start()
        adapter.submitList(viewModel.pagedList)
    }

    private fun expandSearch() {
        rvAllCountries.animate().translationY(0f)
        ibSearch.isSelected = true
        val width = toolbarTitle.width - resources.getDimensionPixelSize(R.dimen.offset_16)
        toolbarTitle.animate().alpha(0f).duration = 200
        sllSearch.elevate(
            resources.getDimension(R.dimen.elevation_1),
            resources.getDimension(R.dimen.elevation_8),
            100
        )
        ValueAnimator.ofInt(
            0,
            width
        ).apply {
            addUpdateListener {
                etSearch.layoutParams.width = animatedValue as Int
                sllSearch.requestLayout()
            }
            doOnEnd {
                etSearch.requestFocus()
                etSearch.setText("")
            }
            duration = 400
        }.start()
        showKeyboard()
    }
}