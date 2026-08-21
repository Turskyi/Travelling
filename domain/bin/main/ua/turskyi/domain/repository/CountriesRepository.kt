package ua.turskyi.domain.repository

import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel

interface CountriesRepository {
    suspend fun refreshCountries(): Result<Unit>
    suspend fun updateSelfie(id: Int, filePath: String): Result<List<CountryModel>>
    suspend fun markAsVisited(country: CountryModel): Result<Unit>
    suspend fun setVisitedModelCountriesFromDb(): Result<List<CountryModel>>
    suspend fun setCities(): Result<List<CityModel>>
    suspend fun setCountNotVisitedCountries(): Result<Int>
    suspend fun removeFromVisited(country: CountryModel): Result<Unit>
    suspend fun removeCity(city: CityModel): Result<Unit>
    suspend fun insertCity(city: CityModel): Result<Unit>
    suspend fun setCountriesByRange(limit: Int, offset: Int): Result<List<CountryModel>>
    suspend fun loadCountriesByNameAndRange(name: String, limit: Int, offset: Int): Result<List<CountryModel>>
}
