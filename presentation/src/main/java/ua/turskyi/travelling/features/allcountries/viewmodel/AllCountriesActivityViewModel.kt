package ua.turskyi.travelling.features.allcountries.viewmodel

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ua.turskyi.domain.interactor.CountriesInteractor
import ua.turskyi.travelling.extensions.mapActualToModel
import ua.turskyi.travelling.features.allcountries.view.adapter.CountriesPositionalDataSource
import ua.turskyi.travelling.features.allcountries.view.adapter.FilteredPositionalDataSource
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.utils.MainThreadExecutor
import java.util.concurrent.Executors

class AllCountriesActivityViewModel(private val interactor: CountriesInteractor) : ViewModel() {

    private val _notVisitedCountriesNumLiveData = MutableLiveData<Int>()
    val notVisitedCountriesNumLiveData: MutableLiveData<Int>
        get() = _notVisitedCountriesNumLiveData

    private val _visibilityLoader = MutableLiveData<Int>()
    var visibilityLoader: MutableLiveData<Int>

    var pagedList: PagedList<Country>

    var searchQuery = ""
        set(value) {
            field = value
            pagedList = getCountryList(value)
        }

    init {
        visibilityLoader = _visibilityLoader
        _visibilityLoader.postValue(View.VISIBLE)
        pagedList = getCountryList(searchQuery)
        viewModelScope.launch {
            getNotVisitedCountriesNumFromDb()
        }
    }

    private fun getCountryList(searchQuery: String): PagedList<Country> {
        val config: PagedList.Config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(20)
            .build()
        val dataSource = CountriesPositionalDataSource(interactor)
        val filteredDataSource =
            FilteredPositionalDataSource(countryName = searchQuery, interactor = interactor)
        return if (searchQuery == "" || searchQuery == "%%") {
            visibilityLoader = dataSource.visibilityLoader
            PagedList.Builder(dataSource, config)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .setNotifyExecutor(MainThreadExecutor())
                .build()
        } else {
            PagedList.Builder(filteredDataSource, config)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .setNotifyExecutor(MainThreadExecutor())
                .build()
        }
    }

    private fun getNotVisitedCountriesNumFromDb() {
        viewModelScope.launch {
            interactor.getNotVisitedCountriesNum({ num ->
                _notVisitedCountriesNumLiveData.postValue(num)
            }, {
                it.printStackTrace()
            })
        }
    }

    fun markAsVisited(country: Country) {
        viewModelScope.launch(Dispatchers.Main) {
            interactor.markAsVisitedCountryModel(country.mapActualToModel()) {
                it.printStackTrace()
            }
        }
    }
}