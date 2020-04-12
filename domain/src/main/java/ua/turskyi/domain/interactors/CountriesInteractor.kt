package ua.turskyi.domain.interactors

import org.koin.core.KoinComponent
import org.koin.core.inject
import ua.turskyi.domain.model.CountryModel
import ua.turskyi.domain.repository.CountriesRepository

class CountriesInteractor : KoinComponent {
    private val repository: CountriesRepository by inject()

    suspend fun getCountriesByRange(
        limit: Int,
        offset: Int,
        onSusses: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        return repository.getCountriesByRange(limit, offset, onSusses, onError)
    }

    suspend fun refreshCountries(
        onSusses: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        return repository.refreshCountriesInDb(onSusses, onError)
    }

    suspend fun getModelCountriesFromDb(
        onSusses: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        return repository.getModelCountriesFromDb(onSusses, onError)
    }

    suspend fun getNotVisitedCountriesNum(
        onSusses: (Int) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        return repository.getNumNotVisitedCountries(onSusses,onError)
    }

    suspend fun getVisitedModelCountries(
        onSusses: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        return repository.getVisitedModelCountriesFromDb(onSusses, onError)
    }

    suspend fun markAsVisitedCountryModel(
        country: CountryModel,
        onError: ((Exception) -> Unit?)? = null
    ) {
        repository.markAsVisited(country, onError = onError)
    }

    suspend fun removeCountryModelFromVisitedList(
        country: CountryModel,
        onError: ((Exception) -> Unit?)? = null
    ) {
        repository.removeFromVisited(country, onError = onError)
    }
}