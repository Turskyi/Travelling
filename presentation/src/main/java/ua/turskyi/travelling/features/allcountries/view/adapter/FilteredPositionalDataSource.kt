package ua.turskyi.travelling.features.allcountries.view.adapter

import android.view.View.GONE
import androidx.lifecycle.MutableLiveData
import androidx.paging.PositionalDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ua.turskyi.domain.interactor.CountriesInteractor
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.utils.extensions.mapModelListToCountryList
import kotlin.coroutines.CoroutineContext

internal class FilteredPositionalDataSource(
    private val countryName: String,
    private val interactor: CountriesInteractor
) : PositionalDataSource<Country>(), CoroutineScope {
    private var job: Job = SupervisorJob()
    // PagedList waits for the initial callback while it is built on the UI thread.
    // Dispatching this work to Main would therefore deadlock the screen launch.
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private val _visibilityLoader = MutableLiveData<Int>()
    val visibilityLoader: MutableLiveData<Int>
        get() = _visibilityLoader

    init {
        addInvalidatedCallback {
            job.cancel()
        }
    }

    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<Country>
    ) {
        launch {
            interactor.loadCountriesByNameAndRange(
                name = countryName,
                limit = params.requestedLoadSize,
                offset = params.requestedStartPosition,
            ).onSuccess { allCountries ->
                callback.onResult(
                    allCountries.mapModelListToCountryList(),
                    params.requestedStartPosition
                )
                _visibilityLoader.postValue(GONE)
            }.onFailure { exception ->
                exception.printStackTrace()
                callback.onResult(emptyList(), params.requestedStartPosition)
                _visibilityLoader.postValue(GONE)
            }
        }
    }

    override fun loadRange(
        params: LoadRangeParams,
        callback: LoadRangeCallback<Country>
    ) {
        launch {
            interactor.loadCountriesByNameAndRange(
                name = countryName,
                limit = params.startPosition + params.loadSize,
                offset = params.startPosition,
            ).onSuccess { allCountries ->
                callback.onResult(allCountries.mapModelListToCountryList())
            }.onFailure { exception ->
                exception.printStackTrace()
                callback.onResult(emptyList())
                _visibilityLoader.postValue(GONE)
            }
        }
    }
}
