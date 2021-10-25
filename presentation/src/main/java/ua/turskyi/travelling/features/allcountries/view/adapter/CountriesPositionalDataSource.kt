package ua.turskyi.travelling.features.allcountries.view.adapter

import android.view.View.GONE
import androidx.lifecycle.MutableLiveData
import androidx.paging.PositionalDataSource
import kotlinx.coroutines.*
import ua.turskyi.domain.interactor.CountriesInteractor
import ua.turskyi.travelling.utils.extensions.mapModelListToCountryList
import ua.turskyi.travelling.models.Country
import java.util.*
import kotlin.concurrent.schedule
import kotlin.coroutines.CoroutineContext

internal class CountriesPositionalDataSource(
    private val interactor: CountriesInteractor,
    private val viewmodelScope: CoroutineScope,
) :
    PositionalDataSource<Country>(), CoroutineScope {

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val _visibilityLoader = MutableLiveData<Int>()
    val visibilityLoader: MutableLiveData<Int>
        get() = _visibilityLoader

    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<Country>
    ) {
        launch {
            interactor.setCountriesByRange(params.requestedLoadSize, params.requestedStartPosition,
                { initCountries ->
                    callback.onResult(
                        initCountries.mapModelListToCountryList(),
                        params.requestedStartPosition
                    )
                    /* creating a little delay of stopping animation for smooth loading*/
                    Timer().schedule(1500) {
                        _visibilityLoader.postValue(GONE)
                    }
                },
                { exception ->
                    exception.printStackTrace()
                    callback.onResult(emptyList(), params.requestedStartPosition)
                    _visibilityLoader.postValue(GONE)
                })
        }
    }

    override fun loadRange(
        params: LoadRangeParams,
        callback: LoadRangeCallback<Country>
    ) {
        viewmodelScope.launch {
            interactor.setCountriesByRange(params.startPosition + params.loadSize,
                params.startPosition,
                { allCountries ->
                    callback.onResult(allCountries.mapModelListToCountryList())
                },
                { exception ->
                    exception.printStackTrace()
                    callback.onResult(emptyList())
                    _visibilityLoader.postValue(GONE)
                })
        }
        job.cancel()
    }
}