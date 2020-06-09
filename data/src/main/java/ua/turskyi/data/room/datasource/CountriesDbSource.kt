package ua.turskyi.data.room.datasource

import ua.turskyi.data.entities.room.CityEntity
import ua.turskyi.data.entities.room.CountryEntity
import ua.turskyi.data.room.dao.CountriesDao

class CountriesDbSource(private val countriesDao: CountriesDao) {
    fun loadCountriesByNameAndRange(name: String?, limit: Int, offset: Int) =
        countriesDao.loadAllCountriesByNameAndRange(name, limit, offset)

    fun getLocalCountriesByRange(limit: Int, offset: Int) =
        countriesDao.getCountriesByRange(limit, offset)

    fun getNumNotVisitedCountries() = countriesDao.getNumNotVisitedCountries()
    fun getVisitedLocalCountriesFromDb() = countriesDao.getVisitedCountries()
    fun getCities() = countriesDao.getCities()
    fun insertCountry(countryEntity: CountryEntity) = countriesDao.insertCountry(countryEntity)
    fun insertAllCountries(countries: List<CountryEntity>) =
        countriesDao.insertAllCountries(countries)

    fun insertCity(cityEntity: CityEntity) = countriesDao.insertCity(cityEntity)
    fun removeCity(city: CityEntity) = countriesDao.delete(city)
    suspend fun updateSelfie(id: Int, selfie: String) = countriesDao.updateSelfie(id, selfie)
}