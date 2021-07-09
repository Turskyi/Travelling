package ua.turskyi.domain.interactor

import kotlinx.coroutines.Job
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
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

    suspend fun downloadCountries(
        onSusses: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.refreshCountries(onSusses, onError)

    suspend fun syncVisitedCountries(
        onSusses: (Job?) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.syncVisitedCountries(onSusses, onError)

    suspend fun getNotVisitedCountriesNum(
        onSusses: (Int) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.getCountNotVisitedCountries(onSusses, onError)

    suspend fun getVisitedModelCountries(
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.getVisitedModelCountriesFromDb(onSuccess, onError)

    suspend fun getCities(
        onSusses: (List<CityModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.getCities(onSusses, onError)

    suspend fun markAsVisitedCountryModel(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)? = null
    ) = repository.markAsVisited(country, onSuccess = onSuccess, onError = onError)

    suspend fun removeCountryModelFromVisitedList(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)? = null
    ) = repository.removeFromVisited(country, onSuccess = onSuccess, onError = onError)

    suspend fun removeCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)? = null
    ) = repository.removeCity(city, onSuccess = onSuccess, onError = onError)

    suspend fun insertCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)? = null
    ) = repository.insertCity(city, onSuccess = onSuccess, onError = onError)
}