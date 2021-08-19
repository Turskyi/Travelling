package ua.turskyi.domain.repository

import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel

interface CountriesRepository {

    suspend fun refreshCountries(onSuccess: () -> Unit, onError: (Exception) -> Unit)

    suspend fun updateSelfie(
        id: Int,
        selfie: String,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun markAsVisited(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun getVisitedModelCountriesFromDb(
        onSuccess: (List<CountryModel>) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun getCities(
        onSuccess: (List<CityModel>) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun setCountNotVisitedCountries(
        onSuccess: (Int) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun removeFromVisited(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun removeCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun insertCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun getCountriesByRange(
        to: Int,
        from: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun loadCountriesByNameAndRange(
        name: String,
        limit: Int,
        offset: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: (Exception) -> Unit
    )
}