package ua.turskyi.travelling.features.allcountries.view.adapter

import androidx.paging.PositionalDataSource
import kotlinx.coroutines.*
import ua.turskyi.domain.interactor.CountriesInteractor
import ua.turskyi.travelling.extensions.mapModelListToCountryList
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
                countryName, params.requestedLoadSize, params.requestedStartPosition,
                { allCountries ->
                    callback.onResult(
                        allCountries.mapModelListToCountryList(),
                        params.requestedStartPosition
                    )
                },
                { exception ->
                    exception.printStackTrace()
                    callback.onResult(emptyList(), params.requestedStartPosition)
                })
            job.cancel()
        }
    }

    override fun loadRange(
        params: LoadRangeParams,
        callback: LoadRangeCallback<Country>
    ) {
        launch {
            interactor.loadCountriesByNameAndRange(
                countryName, params.startPosition + params.loadSize, params.startPosition,
                { allCountries ->
                    /* on next call result returns nothing since only one page of countries required */
                    callback.onResult(allCountries.mapModelListToCountryList())
                },
                {exception ->
                    exception.printStackTrace()
                    callback.onResult(emptyList())
                })
        }
        job.cancel()
    }
}