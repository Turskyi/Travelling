package ua.turskyi.travelling.features.allcountries.view.adapter

import android.util.Log
import androidx.paging.PositionalDataSource
import kotlinx.coroutines.*
import ua.turskyi.domain.interactors.CountriesInteractor
import ua.turskyi.travelling.extensions.mapModelListToActualList
import ua.turskyi.travelling.model.Country
import kotlin.coroutines.CoroutineContext

internal class CountriesPositionalDataSource(
    private val interactor: CountriesInteractor
) : PositionalDataSource<Country>(), CoroutineScope {
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<Country>
    ) {
        launch {
            interactor.getCountriesByRange(params.requestedLoadSize, 0,
                { allCountries ->
                    callback.onResult(allCountries.mapModelListToActualList(), 0)
                },
                {
                    callback.onResult(emptyList(), 0)
                })
            job.cancel()
        }
    }

    override fun loadRange(
        params: LoadRangeParams,
        callback: LoadRangeCallback<Country>
    ) {
        Log.d("loadRange", "${params.loadSize} \\ ${params.startPosition}")
        GlobalScope.launch {
            interactor.getCountriesByRange(params.loadSize, params.startPosition,
                { allCountries ->
                    callback.onResult(allCountries.mapModelListToActualList())
                },
                {
                    callback.onResult(emptyList())
                })
        }
        job.cancel()
    }
}