package ua.turskyi.domain.repository

import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel

interface CountriesRepository {
    val isSynchronized: Boolean
    var isUpgraded: Boolean

    suspend fun refreshCountriesInDb(
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun syncVisitedCountries(
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun updateSelfie(
        id: Int,
        selfie: String,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun markAsVisited(
        country: CountryModel,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun getVisitedModelCountriesFromDb(
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun getCities(
        onSuccess: (List<CityModel>) -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun getCountNotVisitedCountries(
        onSuccess: (Int) -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun removeFromVisited(
        country: CountryModel,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun removeCity(
        city: CityModel,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun insertCity(
        city: CityModel,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun getCountriesByRange(
        to: Int,
        from: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun loadCountriesByNameAndRange(
        name: String?,
        limit: Int,
        offset: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )
}