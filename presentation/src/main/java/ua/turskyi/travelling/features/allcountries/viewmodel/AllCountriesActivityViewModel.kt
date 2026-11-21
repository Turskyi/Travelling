package ua.turskyi.travelling.features.allcountries.viewmodel

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import ua.turskyi.domain.interactor.CountriesInteractor
import ua.turskyi.travelling.features.allcountries.view.adapter.CountriesPagingSource
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.utils.Event
import ua.turskyi.travelling.utils.extensions.mapToModel

class AllCountriesActivityViewModel(private val interactor: CountriesInteractor) : ViewModel() {

    val notVisitedCountriesNumLiveData: LiveData<Int>
        field = MutableLiveData<Int>()

    private var _visibilityLoader = MediatorLiveData<Int>()
    val visibilityLoader: LiveData<Int>
        get() = _visibilityLoader

    private var currentVisibilitySource: LiveData<Int>? = null

    private val searchQueryFlow = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedCountries: Flow<PagingData<Country>> = searchQueryFlow
        .flatMapLatest { query ->
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    initialLoadSize = 20,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = {
                    val pagingSource = CountriesPagingSource(interactor, query)
                    updateVisibilitySource(pagingSource.visibilityLoader)
                    pagingSource
                }
            ).flow
        }.cachedIn(viewModelScope)

    var searchQuery = ""
        set(value) {
            field = value
            searchQueryFlow.value = value
        }

    val errorMessage: LiveData<Event<String>>
        field = MutableLiveData<Event<String>>()

    init {
        _visibilityLoader.postValue(VISIBLE)
        getNotVisitedCountriesNum()
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
                notVisitedCountriesNumLiveData.postValue(num)
            }.onFailure { exception ->
                _visibilityLoader.postValue(GONE)
                errorMessage.postValue(Event(exception.localizedMessage ?: exception.toString()))
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
                errorMessage.postValue(Event(exception.localizedMessage ?: exception.toString()))
            }
        }
    }
}
