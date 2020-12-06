package ua.turskyi.travelling.features.allcountries.viewmodel

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.LiveData
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
import ua.turskyi.travelling.utils.Event
import ua.turskyi.travelling.utils.MainThreadExecutor
import java.util.concurrent.Executors

class AllCountriesActivityViewModel(private val interactor: CountriesInteractor) : ViewModel() {

    private val _notVisitedCountriesNumLiveData = MutableLiveData<Int>()
    val notVisitedCountriesNumLiveData: MutableLiveData<Int>
        get() = _notVisitedCountriesNumLiveData

    private var _visibilityLoader = MutableLiveData<Int>()
    val visibilityLoader: LiveData<Int>
        get() = _visibilityLoader

    var pagedList: PagedList<Country>

    var searchQuery = ""
        set(value) {
            field = value
            pagedList = getCountryList(value)
        }

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>>
        get() = _errorMessage

    init {
        _visibilityLoader.postValue(VISIBLE)
        pagedList = getCountryList(searchQuery)
        viewModelScope.launch {
            getNotVisitedCountriesNumFromDb()
        }
    }

    private fun getCountryList(searchQuery: String): PagedList<Country> {
        val config: PagedList.Config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(20)
            .setPageSize(20)
            .build()
        val dataSource = CountriesPositionalDataSource(interactor)
        val filteredDataSource =
            FilteredPositionalDataSource(countryName = searchQuery, interactor = interactor)
        _visibilityLoader = dataSource.visibilityLoader
        return if (searchQuery == "" || searchQuery == "%%") {
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
            }, { exception ->
                exception.printStackTrace()
                _visibilityLoader.postValue(GONE)
                _errorMessage.run {
                    exception.message?.let { message ->
                        /* Trigger the event by setting a new Event as a new value */
                        postValue(Event(message))
                    }
                }
            })
        }
    }

    fun markAsVisited(country: Country) {
        viewModelScope.launch(Dispatchers.Main) {
            interactor.markAsVisitedCountryModel(country.mapActualToModel()) { exception ->
                exception.printStackTrace()
                _visibilityLoader.postValue(GONE)
                _errorMessage.run {
                    exception.message?.let { message ->
                        /* Trigger the event by setting a new Event as a new value */
                        postValue(Event(message))
                    }
                }
            }
        }
    }
}