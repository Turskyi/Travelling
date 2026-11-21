package ua.turskyi.travelling.features.travellers.view.adapter

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import ua.turskyi.domain.interactor.TravellersInteractor
import ua.turskyi.domain.model.TravellerModel
import ua.turskyi.travelling.entities.Traveller
import ua.turskyi.travelling.utils.extensions.mapModelListToTravellerList
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class TravellersPagingSource(
    private val interactor: TravellersInteractor,
    private val searchQuery: String,
) : PagingSource<Int, Traveller>() {

    private val _visibilityLoader = MutableLiveData<Int>()
    val visibilityLoader: MutableLiveData<Int>
        get() = _visibilityLoader

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Traveller> {
        val position = params.key ?: 0
        val loadSize = params.loadSize
        if (params is LoadParams.Refresh) {
            _visibilityLoader.postValue(VISIBLE)
        }

        return try {
            val travellers = suspendCancellableCoroutine { continuation ->
                val onSuccess: (List<TravellerModel>) -> Unit = { models ->
                    continuation.resume(models)
                }
                val onError: (Exception) -> Unit = { exception ->
                    continuation.resumeWithException(exception)
                }
                val requestedEndPosition = (position + loadSize).toLong()

                if (searchQuery.isBlank()) {
                    interactor.setTravellersByRange(
                        requestedLoadSize = requestedEndPosition,
                        requestedStartPosition = position,
                        onSuccess = onSuccess,
                        onError = onError,
                    )
                } else {
                    interactor.setTravellersByName(
                        name = searchQuery,
                        requestedLoadSize = requestedEndPosition,
                        requestedStartPosition = position,
                        onSuccess = onSuccess,
                        onError = onError,
                    )
                }
            }
            _visibilityLoader.postValue(GONE)

            val data = travellers.mapModelListToTravellerList()
            LoadResult.Page(
                data = data,
                prevKey = if (position == 0) null else maxOf(0, position - loadSize),
                nextKey = if (data.size < loadSize) null else position + data.size,
            )
        } catch (exception: Exception) {
            _visibilityLoader.postValue(GONE)
            if (exception is CancellationException) throw exception
            exception.printStackTrace()
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Traveller>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(state.config.pageSize)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(state.config.pageSize)
        }
    }
}
