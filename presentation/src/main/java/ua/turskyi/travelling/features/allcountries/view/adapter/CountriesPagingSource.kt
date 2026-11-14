package ua.turskyi.travelling.features.allcountries.view.adapter

import android.view.View.GONE
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import ua.turskyi.domain.interactor.CountriesInteractor
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.utils.extensions.mapModelListToCountryList

internal class CountriesPagingSource(
    private val interactor: CountriesInteractor,
    private val searchQuery: String = ""
) : PagingSource<Int, Country>() {

    private val _visibilityLoader = MutableLiveData<Int>()
    val visibilityLoader: MutableLiveData<Int>
        get() = _visibilityLoader

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Country> {
        val position = params.key ?: 0
        val loadSize = params.loadSize

        return try {
            val result = if (searchQuery.isBlank() || searchQuery == "%%") {
                interactor.setCountriesByRange(
                    limit = loadSize,
                    offset = position
                )
            } else {
                interactor.loadCountriesByNameAndRange(
                    name = searchQuery,
                    limit = loadSize,
                    offset = position
                )
            }

            result.onSuccess {
                _visibilityLoader.postValue(GONE)
            }.onFailure {
                _visibilityLoader.postValue(GONE)
            }

            if (result.isFailure) {
                return LoadResult.Error(result.exceptionOrNull() ?: RuntimeException("Unknown error"))
            }

            val countries = result.getOrNull()?.mapModelListToCountryList() ?: emptyList()

            val nextKey = if (countries.isEmpty() || countries.size < loadSize) null else position + countries.size
            val prevKey = if (position == 0) null else maxOf(0, position - loadSize)

            LoadResult.Page(
                data = countries,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (exception: Exception) {
            exception.printStackTrace()
            _visibilityLoader.postValue(GONE)
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Country>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(state.config.pageSize)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(state.config.pageSize)
        }
    }
}
