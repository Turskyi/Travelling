package ua.turskyi.travelling.features.allcountries.viewmodel

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ua.turskyi.domain.interactor.CountriesInteractor
import ua.turskyi.travelling.features.allcountries.view.adapter.CountriesPositionalDataSource
import ua.turskyi.travelling.features.allcountries.view.adapter.FilteredPositionalDataSource
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.utils.Event
import ua.turskyi.travelling.utils.MainThreadExecutor
import ua.turskyi.travelling.utils.extensions.mapToModel
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
        viewModelScope.launch { getNotVisitedCountriesNum() }
    }

    private fun getCountryList(searchQuery: String): PagedList<Country> {

        // PagedList
        val config: PagedList.Config = PagedList.Config.Builder()
            /* If "true", then it should be created another viewType in Adapter "onCreateViewHolder"
              while uploading */
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(20)
            .setPageSize(20)
            .build()

        return if (searchQuery == "" || searchQuery == "%%") {
            // DataSource
            val dataSource = CountriesPositionalDataSource(interactor, viewModelScope)
            _visibilityLoader = dataSource.visibilityLoader
            PagedList.Builder(dataSource, config)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .setNotifyExecutor(MainThreadExecutor())
                .build()
        } else {
            val filteredDataSource = FilteredPositionalDataSource(countryName = searchQuery, interactor = interactor)
            PagedList.Builder(filteredDataSource, config)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .setNotifyExecutor(MainThreadExecutor())
                .build()
        }
    }

    private fun getNotVisitedCountriesNum(): Job {
        return viewModelScope.launch {
            interactor.setNotVisitedCountriesNum({ num: Int ->
                _notVisitedCountriesNumLiveData.postValue(num)
            }, { exception: Exception ->
                _visibilityLoader.postValue(GONE)
                // Trigger the event by setting a new Event as a new value
                _errorMessage.postValue(Event(exception.localizedMessage ?: exception.stackTraceToString()))
            })
        }
    }

    fun markAsVisited(country: Country, onSuccess: () -> Unit) {
        _visibilityLoader.postValue(VISIBLE)
        viewModelScope.launch(Dispatchers.IO) {
            interactor.markAsVisitedCountryModel(country.mapToModel(), {
                viewModelScope.launch(Dispatchers.Main) {
                    onSuccess()
                }
            }, { exception: Exception ->
                _visibilityLoader.postValue(GONE)
                // Trigger the event by setting a new Event as a new value
                _errorMessage.postValue(Event(exception.localizedMessage ?: exception.stackTraceToString()))
            })
        }
    }
}