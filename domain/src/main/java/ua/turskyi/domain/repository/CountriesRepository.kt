package ua.turskyi.domain.repository

import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel

interface CountriesRepository {
    suspend fun refreshCountriesInDb(
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun updateSelfie(
        id: Int,
        selfie: String,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun addModelsToDb(
        countries: MutableList<CountryModel>,
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
        onSuccess: (MutableList<CityModel>) -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun getNumNotVisitedCountries(
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
        limit: Int,
        offset: Int,
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