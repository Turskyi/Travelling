package ua.turskyi.travelling.features.allcountries.viewmodel

import android.view.View
import android.view.View.GONE
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ua.turskyi.domain.interactors.CountriesInteractor
import ua.turskyi.travelling.extensions.mapActualToModel
import ua.turskyi.travelling.extensions.mapModelListToActualList
import ua.turskyi.travelling.features.allcountries.view.adapter.CountriesPositionalDataSource
import ua.turskyi.travelling.model.Country
import ua.turskyi.visitedcountries.utils.MainThreadExecutor
import java.util.concurrent.Executors

class AllCountriesActivityViewModel(private val interactor: CountriesInteractor) : ViewModel() {

    private val _countriesLiveData = MutableLiveData<List<Country>>()
    val countriesLiveData: MutableLiveData<List<Country>>
        get() = _countriesLiveData

    private val _visibilityLoader = MutableLiveData<Int>()
    val visibilityLoader: MutableLiveData<Int>
        get() = _visibilityLoader

    private val _pagedList = MutableLiveData<PagedList<Country>>()
    val pagedList: MutableLiveData<PagedList<Country>>

    init {
        pagedList = _pagedList
        viewModelScope.launch {
            _visibilityLoader.postValue(View.VISIBLE)
            getCountriesFromDb()
        }
    }

    private fun getCountriesFromDb() {
        viewModelScope.launch {
            interactor.getModelCountriesFromDb({ countries ->
                _countriesLiveData.postValue(countries.mapModelListToActualList())
                val dataSource = CountriesPositionalDataSource(interactor)
                val config: PagedList.Config = PagedList.Config.Builder()
                    .setEnablePlaceholders(false)
                    .setPageSize(20)
                    .build()
                val pagedList: PagedList<Country> = PagedList.Builder(dataSource, config)
                    .setFetchExecutor(Executors.newSingleThreadExecutor())
                    .setNotifyExecutor(MainThreadExecutor())
                    .build()
                _pagedList.postValue(pagedList)
                _visibilityLoader.postValue(GONE)
            }, {
                _visibilityLoader.postValue(GONE)
            })
        }
    }

    fun markAsVisited(country: Country) {
        viewModelScope.launch(Dispatchers.Main) {
            interactor.markAsVisitedCountryModel(country.mapActualToModel()) {
                //            TODO: What to do if Country did not got to visited?
            }
        }
    }
}