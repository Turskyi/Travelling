package ua.turskyi.travelling.features.allcountries.view.adapter

import android.view.View.GONE
import androidx.lifecycle.MutableLiveData
import androidx.paging.PositionalDataSource
import kotlinx.coroutines.*
import ua.turskyi.domain.interactor.CountriesInteractor
import ua.turskyi.travelling.extensions.mapModelListToActualList
import ua.turskyi.travelling.models.Country
import java.util.*
import kotlin.concurrent.schedule
import kotlin.coroutines.CoroutineContext

internal class CountriesPositionalDataSource(private val interactor: CountriesInteractor) :
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
            interactor.getCountriesByRange(params.requestedLoadSize, 0,
                { initCountries ->
                    callback.onResult(initCountries.mapModelListToActualList(), 0)
                 /* a little bit delay of stopping animation */
                    Timer().schedule(2000) {
                        _visibilityLoader.postValue(GONE)
                    }
                },
                {
                    it.printStackTrace()
                    callback.onResult(emptyList(), 0)
                    _visibilityLoader.postValue(GONE)
                })
            job.cancel()
        }
    }

    override fun loadRange(
        params: LoadRangeParams,
        callback: LoadRangeCallback<Country>
    ) {
        GlobalScope.launch {
            interactor.getCountriesByRange(params.loadSize, params.startPosition,
                { allCountries ->
                    callback.onResult(allCountries.mapModelListToActualList())
                },
                {
                    it.printStackTrace()
                    callback.onResult(emptyList())
                    _visibilityLoader.postValue(GONE)
                })
        }
        job.cancel()
    }
}