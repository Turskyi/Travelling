package ua.turskyi.data.datasources.local.firestore

import ua.turskyi.data.datasources.local.entities.TravellerEntity

interface FirestoreDatabaseSource {
    suspend fun setTravellersByRange(
        to: Long,
        from: Int,
        onSuccess: (List<TravellerEntity>) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun setTravellersByName(
        nameQuery: String,
        requestedLoadSize: Long,
        requestedStartPosition: Int,
        onSuccess: (List<TravellerEntity>) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun setTopTravellersPercent(onSuccess: (Int) -> Unit, onError: (Exception) -> Unit)
    suspend fun saveTraveller(onSuccess: () -> Unit, onError: (Exception) -> Unit)
    suspend fun setUserVisibility(
        visible: Boolean,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun setUserVisibility(onSuccess: (Boolean) -> Unit, onError: (Exception) -> Unit)
}
