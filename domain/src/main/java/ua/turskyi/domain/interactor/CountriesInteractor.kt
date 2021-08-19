package ua.turskyi.domain.interactor

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel
import ua.turskyi.domain.repository.CountriesRepository

class CountriesInteractor : KoinComponent {
    private val repository: CountriesRepository by inject()

    suspend fun loadCountriesByNameAndRange(
        name: String,
        limit: Int,
        offset: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = repository.loadCountriesByNameAndRange(name, limit, offset, onSuccess, onError)

    suspend fun updateSelfie(
        id: Int,
        selfie: String,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = repository.updateSelfie(id, selfie, onSuccess, onError)

    suspend fun getCountriesByRange(
        limit: Int,
        offset: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = repository.getCountriesByRange(limit, offset, onSuccess, onError)

    suspend fun downloadCountries(
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = repository.refreshCountries(onSuccess, onError)

    suspend fun setNotVisitedCountriesNum(
        onSuccess: (Int) -> Unit,
        onError: (Exception) -> Unit
    ) = repository.setCountNotVisitedCountries(onSuccess, onError)

    suspend fun setVisitedModelCountries(
        onSuccess: (List<CountryModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = repository.getVisitedModelCountriesFromDb(onSuccess, onError)

    suspend fun setCities(
        onSuccess: (List<CityModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = repository.getCities(onSuccess, onError)

    suspend fun markAsVisitedCountryModel(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = repository.markAsVisited(country, onSuccess = onSuccess, onError = onError)

    suspend fun removeCountryModelFromVisitedList(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = repository.removeFromVisited(country, onSuccess = onSuccess, onError = onError)

    suspend fun removeCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = repository.removeCity(city, onSuccess = onSuccess, onError = onError)

    suspend fun insertCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = repository.insertCity(city, onSuccess = onSuccess, onError = onError)
}