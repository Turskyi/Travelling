package ua.turskyi.domain.repository

import ua.turskyi.domain.model.CountryModel

interface CountriesRepository {
    suspend fun refreshCountriesInDb(
        onSusses: () -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun getModelCountriesFromDb(
        onSusses: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun addModelCountriesToDb(
        countries: List<CountryModel>,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun markAsVisited(
        country: CountryModel,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun getVisitedModelCountriesFromDb(
        onSusses: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun getNumNotVisitedCountries(
        onSusses: (Int) -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun removeFromVisited(
        country: CountryModel,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun getCountriesByRange(
        limit: Int,
        offset: Int,
        onSusses: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )
}