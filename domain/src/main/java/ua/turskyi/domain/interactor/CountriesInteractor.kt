package ua.turskyi.domain.interactor

import org.koin.core.KoinComponent
import org.koin.core.inject
import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel
import ua.turskyi.domain.repository.CountriesRepository

class CountriesInteractor : KoinComponent {
    private val repository: CountriesRepository by inject()

    suspend fun loadCountriesByNameAndRange(
        name: String?,
        limit: Int,
        offset: Int,
        onSusses: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        return repository.loadCountriesByNameAndRange(name, limit, offset, onSusses, onError)
    }

    suspend fun updateSelfie(
        id: Int,
        selfie: String,
        onSusses: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        return repository.updateSelfie(id, selfie, onSusses, onError)
    }

    suspend fun getCountriesByRange(
        limit: Int,
        offset: Int,
        onSusses: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.getCountriesByRange(limit, offset, onSusses, onError)

    suspend fun refreshCountries(
        onSusses: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        return repository.refreshCountriesInDb(onSusses, onError)
    }

    suspend fun getNotVisitedCountriesNum(
        onSusses: (Int) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.getNumNotVisitedCountries(onSusses, onError)

    suspend fun getVisitedModelCountries(
        onSusses: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        return repository.getVisitedModelCountriesFromDb(onSusses, onError)
    }

    suspend fun getCities(
        onSusses: (MutableList<CityModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        return repository.getCities(onSusses, onError)
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

    suspend fun removeCity(
        city: CityModel,
        onError: ((Exception) -> Unit?)? = null
    ) {
        repository.removeCity(city, onError = onError)
    }

    suspend fun insertCity(
        city: CityModel,
        onError: ((Exception) -> Unit?)? = null
    ) {
        repository.insertCity(city, onError = onError)
    }
}