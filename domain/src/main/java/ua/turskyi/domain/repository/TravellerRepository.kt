package ua.turskyi.domain.repository

import ua.turskyi.domain.model.TravellerModel

interface TravellerRepository {
    fun setTravellersByName(
        name: String,
        requestedLoadSize: Long,
        requestedStartPosition: Int,
        onSuccess: (List<TravellerModel>) -> Unit,
        onError: (Exception) -> Unit
    )

    fun setTopTravellersPercent(onSuccess: (Int) -> Unit, onError: (Exception) -> Unit)
    fun saveTraveller(onSuccess: () -> Unit, onError: (Exception) -> Unit)
    fun setTravellersByRange(
        requestedLoadSize: Long,
        requestedStartPosition: Int,
        onSuccess: (List<TravellerModel>) -> Unit,
        onError: (Exception) -> Unit
    )

    fun setUserVisibility(visible: Boolean, onSuccess: () -> Unit, onError: (Exception) -> Unit)
    fun setUserVisibility(onSuccess: (Boolean) -> Unit, onError: (Exception) -> Unit)
}
