package ua.turskyi.domain.interactor

import org.koin.core.KoinComponent
import org.koin.core.inject
import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel
import ua.turskyi.domain.repository.CountriesRepository

class CountriesInteractor : KoinComponent {
    private val repository: CountriesRepository by inject()

    var isSynchronized = repository.isSynchronized
    var isUpgraded: Boolean
        get() = repository.isUpgraded
        set(isSynchronized) {
            repository.isUpgraded = isSynchronized
        }

    suspend fun loadCountriesByNameAndRange(
        name: String?,
        limit: Int,
        offset: Int,
        onSusses: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.loadCountriesByNameAndRange(name, limit, offset, onSusses, onError)

    suspend fun updateSelfie(
        id: Int,
        selfie: String,
        onSusses: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.updateSelfie(id, selfie, onSusses, onError)

    suspend fun getCountriesByRange(
        limit: Int,
        offset: Int,
        onSusses: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.getCountriesByRange(limit, offset, onSusses, onError)

    suspend fun refreshCountries(
        onSusses: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.refreshCountriesInDb(onSusses, onError)

    suspend fun syncVisitedCountries(
        onSusses: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.syncVisitedCountries(onSusses, onError)

    suspend fun getNotVisitedCountriesNum(
        onSusses: (Int) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.getCountNotVisitedCountries(onSusses, onError)

    suspend fun getVisitedModelCountries(
        onSusses: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.getVisitedModelCountriesFromDb(onSusses, onError)

    suspend fun getCities(
        onSusses: (List<CityModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.getCities(onSusses, onError)

    suspend fun markAsVisitedCountryModel(
        country: CountryModel,
        onError: ((Exception) -> Unit?)? = null
    ) = repository.markAsVisited(country, onError = onError)

    suspend fun removeCountryModelFromVisitedList(
        country: CountryModel,
        onError: ((Exception) -> Unit?)? = null
    ) = repository.removeFromVisited(country, onError = onError)

    suspend fun removeCity(
        city: CityModel,
        onError: ((Exception) -> Unit?)? = null
    ) = repository.removeCity(city, onError = onError)

    suspend fun insertCity(
        city: CityModel,
        onError: ((Exception) -> Unit?)? = null
    ) = repository.insertCity(city, onError = onError)
}