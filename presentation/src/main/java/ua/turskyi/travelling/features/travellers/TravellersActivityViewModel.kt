@file:OptIn(ExperimentalCoroutinesApi::class)

package ua.turskyi.travelling.features.travellers

import android.view.View.GONE
import android.view.View.INVISIBLE
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import ua.turskyi.domain.interactor.TravellersInteractor
import ua.turskyi.travelling.entities.Traveller
import ua.turskyi.travelling.features.travellers.view.adapter.TravellersPagingSource
import ua.turskyi.travelling.utils.Event

class TravellersActivityViewModel(private val interactor: TravellersInteractor) : ViewModel() {

    private val _topTravellersPercentLiveData: MutableLiveData<Int> = MutableLiveData<Int>()
    val topTravellersPercentLiveData: MutableLiveData<Int>
        get() = _topTravellersPercentLiveData

    val visibilityLoader: LiveData<Int>
        field: MediatorLiveData<Int> = MediatorLiveData<Int>()

    private var currentVisibilitySource: LiveData<Int>? = null

    private var _visibilityUser: MutableLiveData<Int> = MutableLiveData<Int>()
    val visibilityUser: LiveData<Int>
        get() = _visibilityUser

    private val searchQueryFlow = MutableStateFlow("")
    private val refreshTrigger = MutableStateFlow(0)

    val pagedTravellers: Flow<PagingData<Traveller>> = combine(
        searchQueryFlow,
        refreshTrigger,
    ) { query, _ -> query }
        .flatMapLatest { query ->
            Pager(
                config = PagingConfig(
                    pageSize = if (query.isBlank()) 10 else 1,
                    initialLoadSize = 10,
                    enablePlaceholders = false,
                ),
                pagingSourceFactory = {
                    val pagingSource = TravellersPagingSource(interactor, query)
                    updateVisibilitySource(pagingSource.visibilityLoader)
                    pagingSource
                },
            ).flow
        }
        .cachedIn(viewModelScope)

    var searchQuery = ""
        set(value) {
            field = value
            searchQueryFlow.value = value
        }

    val errorMessage: LiveData<Event<String>>
        field: MutableLiveData<Event<String>> = MutableLiveData<Event<String>>()

    init {
        visibilityLoader.postValue(VISIBLE)

        viewModelScope.launch {
            setTopTravellersPercent()
        }

        interactor.setUserVisibility(
            onSuccess = { isVisible: Boolean ->
                if (isVisible) {
                    _visibilityUser.postValue(VISIBLE)
                } else {
                    _visibilityUser.postValue(INVISIBLE)
                }
            },
            onError = { exception: Exception ->
                visibilityLoader.postValue(GONE)
                errorMessage.postValue(
                    Event(exception.localizedMessage ?: exception.stackTraceToString()),
                )
            },
        )
    }

    private fun updateVisibilitySource(source: LiveData<Int>) {
        currentVisibilitySource?.let { visibilityLoader.removeSource(it) }
        currentVisibilitySource = source
        visibilityLoader.addSource(source) { currentVisibility ->
            visibilityLoader.value = currentVisibility
        }
    }

    private fun setTopTravellersPercent() = viewModelScope.launch {
        interactor.setTopTravellersPercent(
            onSuccess = { percent: Int ->
                _topTravellersPercentLiveData.postValue(percent)
            },
            onError = { exception: Exception ->
                visibilityLoader.postValue(GONE)
                errorMessage.postValue(
                    Event(exception.localizedMessage ?: exception.stackTraceToString()),
                )
            },
        )
    }

    fun onBecomingVisibleTriggered() {
        interactor.setUserVisibility(
            isVisible = true,
            onSuccess = {
                refreshTravellers()
                _visibilityUser.postValue(VISIBLE)
            },
            onError = { exception: Exception ->
                visibilityLoader.postValue(GONE)
                errorMessage.postValue(
                    Event(exception.localizedMessage ?: exception.stackTraceToString()),
                )
            },
        )
    }

    fun onVisibilityFabClicked() {
        interactor.setUserVisibility(
            isVisible = false,
            onSuccess = {
                refreshTravellers()
                _visibilityUser.postValue(INVISIBLE)
            },
            onError = { exception: Exception ->
                visibilityLoader.postValue(GONE)
                errorMessage.postValue(
                    Event(exception.localizedMessage ?: exception.stackTraceToString()),
                )
            },
        )
    }

    private fun refreshTravellers() {
        refreshTrigger.value += 1
    }
}
