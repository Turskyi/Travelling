package ua.turskyi.travelling.features.allcountries.viewmodel

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.launch
import ua.turskyi.domain.interactor.CountriesInteractor
import ua.turskyi.travelling.features.allcountries.view.adapter.CountriesPositionalDataSource
import ua.turskyi.travelling.features.allcountries.view.adapter.FilteredPositionalDataSource
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.utils.Event
import ua.turskyi.travelling.utils.MainThreadExecutor
import ua.turskyi.travelling.utils.extensions.mapToModel

class AllCountriesActivityViewModel(private val interactor: CountriesInteractor) : ViewModel() {

    private val _notVisitedCountriesNumLiveData = MutableLiveData<Int>()
    val notVisitedCountriesNumLiveData: MutableLiveData<Int>
        get() = _notVisitedCountriesNumLiveData

    private var _visibilityLoader = MediatorLiveData<Int>()
    val visibilityLoader: LiveData<Int>
        get() = _visibilityLoader

    private var currentVisibilitySource: LiveData<Int>? = null

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
        getNotVisitedCountriesNum()
    }

    private fun getCountryList(searchQuery: String): PagedList<Country> {

        // PagedList
        val config: PagedList.Config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(20)
            .setPageSize(20)
            .build()

        return if (searchQuery == "" || searchQuery == "%%") {
            // DataSource
            val dataSource = CountriesPositionalDataSource(interactor)
            updateVisibilitySource(dataSource.visibilityLoader)
            PagedList.Builder(dataSource, config)
                .setFetchExecutor(Dispatchers.IO.asExecutor())
                .setNotifyExecutor(MainThreadExecutor())
                .build()
        } else {
            val filteredDataSource = FilteredPositionalDataSource(countryName = searchQuery, interactor = interactor)
            updateVisibilitySource(filteredDataSource.visibilityLoader)
            PagedList.Builder(filteredDataSource, config)
                .setFetchExecutor(Dispatchers.IO.asExecutor())
                .setNotifyExecutor(MainThreadExecutor())
                .build()
        }
    }

    private fun updateVisibilitySource(source: LiveData<Int>) {
        currentVisibilitySource?.let { _visibilityLoader.removeSource(it) }
        currentVisibilitySource = source
        _visibilityLoader.addSource(source) {
            _visibilityLoader.value = it
        }
    }

    private fun getNotVisitedCountriesNum() {
        viewModelScope.launch {
            interactor.setNotVisitedCountriesNum().onSuccess { num ->
                _notVisitedCountriesNumLiveData.postValue(num)
            }.onFailure { exception ->
                _visibilityLoader.postValue(GONE)
                _errorMessage.postValue(Event(exception.localizedMessage ?: exception.toString()))
            }
        }
    }

    fun markAsVisited(country: Country, onSuccess: () -> Unit) {
        _visibilityLoader.postValue(VISIBLE)
        viewModelScope.launch(Dispatchers.IO) {
            interactor.markAsVisitedCountryModel(country.mapToModel()).onSuccess {
                viewModelScope.launch(Dispatchers.Main) {
                    onSuccess()
                }
            }.onFailure { exception ->
                _visibilityLoader.postValue(GONE)
                _errorMessage.postValue(Event(exception.localizedMessage ?: exception.toString()))
            }
        }
    }

}
