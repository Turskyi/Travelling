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
        offset: Int
    ) = repository.loadCountriesByNameAndRange(name, limit, offset)

    suspend fun updateSelfie(
        id: Int,
        filePath: String
    ) = repository.updateSelfie(id, filePath)

    suspend fun setCountriesByRange(
        limit: Int,
        offset: Int
    ) = repository.setCountriesByRange(limit, offset)

    suspend fun downloadCountries() = repository.refreshCountries()

    suspend fun setNotVisitedCountriesNum() = repository.setCountNotVisitedCountries()

    suspend fun setVisitedCountries() = repository.setVisitedModelCountriesFromDb()

    suspend fun setCities() = repository.setCities()

    suspend fun markAsVisitedCountryModel(
        country: CountryModel
    ) = repository.markAsVisited(country)

    suspend fun removeCountryModelFromVisitedList(
        country: CountryModel
    ) = repository.removeFromVisited(country)

    suspend fun removeCity(
        city: CityModel
    ) = repository.removeCity(city)

    suspend fun insertCity(
        city: CityModel
    ) = repository.insertCity(city)
}
