package ua.turskyi.travelling.features.allcountries.view.adapter

import androidx.paging.PositionalDataSource
import kotlinx.coroutines.*
import ua.turskyi.domain.interactors.CountriesInteractor
import ua.turskyi.travelling.extensions.mapModelListToActualList
import ua.turskyi.travelling.models.Country
import kotlin.coroutines.CoroutineContext

internal class FilteredPositionalDataSource(
    private val countryName: String?,
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
            interactor.loadCountriesByNameAndRange(
                countryName, params.requestedLoadSize, 0,
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
        GlobalScope.launch {
            interactor.loadCountriesByNameAndRange(
                countryName, params.loadSize, params.startPosition,
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