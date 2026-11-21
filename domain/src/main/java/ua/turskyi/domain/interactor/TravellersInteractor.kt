package ua.turskyi.domain.interactor

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ua.turskyi.domain.model.TravellerModel
import ua.turskyi.domain.repository.TravellerRepository

@Suppress("unused")
class TravellersInteractor : KoinComponent {
    private val repository: TravellerRepository by inject()

    fun setTravellersByName(
        name: String,
        requestedLoadSize: Long,
        requestedStartPosition: Int,
        onSuccess: (List<TravellerModel>) -> Unit,
        onError: ((Exception) -> Unit)
    ) = repository.setTravellersByName(
        name,
        requestedLoadSize,
        requestedStartPosition,
        onSuccess,
        onError,
    )

    fun setTopTravellersPercent(onSuccess: (Int) -> Unit, onError: (Exception) -> Unit) {
        repository.setTopTravellersPercent(onSuccess, onError)
    }

    fun setTravellersByRange(
        requestedLoadSize: Long,
        requestedStartPosition: Int,
        onSuccess: (List<TravellerModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = repository.setTravellersByRange(
        requestedLoadSize,
        requestedStartPosition,
        onSuccess,
        onError,
    )

    fun setUserVisibility(isVisible: Boolean, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        repository.setUserVisibility(
            isVisible,
            onSuccess,
            onError,
        )
    }
    fun setUserVisibility(onSuccess: (Boolean) -> Unit, onError: (Exception) -> Unit) {
        repository.setUserVisibility(onSuccess, onError)
    }
}
