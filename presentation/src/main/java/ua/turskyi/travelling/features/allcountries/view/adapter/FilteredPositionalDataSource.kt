package ua.turskyi.travelling.features.allcountries.view.adapter

import androidx.paging.PositionalDataSource
import kotlinx.coroutines.*
import ua.turskyi.domain.interactor.CountriesInteractor
import ua.turskyi.travelling.utils.extensions.mapModelListToCountryList
import ua.turskyi.travelling.models.Country
import kotlin.coroutines.CoroutineContext

internal class FilteredPositionalDataSource(
    private val countryName: String,
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
                name = countryName,
                limit = params.requestedLoadSize,
                offset = params.requestedStartPosition,
                onSuccess = { allCountries ->
                    callback.onResult(
                        allCountries.mapModelListToCountryList(),
                        params.requestedStartPosition
                    )
                },
                onError = { exception ->
                    exception.printStackTrace()
                    callback.onResult(emptyList(), params.requestedStartPosition)
                },
            )
            job.cancel()
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
                onSuccess = { allCountries ->
                    /* on next call result returns nothing
                     since only one page of countries required */
                    callback.onResult(allCountries.mapModelListToCountryList())
                },
                onError = { exception ->
                    exception.printStackTrace()
                    callback.onResult(emptyList())
                },
            )
        }
        job.cancel()
    }
}