package ua.turskyi.data.room.datasource

import ua.turskyi.data.entities.room.CityEntity
import ua.turskyi.data.entities.room.CountryEntity
import ua.turskyi.data.room.dao.CountriesDao

class CountriesDbSource(private val countriesDao: CountriesDao) {
    fun getNumNotVisitedCountries() = countriesDao.getNumNotVisitedCountries()
    fun getVisitedLocalCountriesFromDb() = countriesDao.getVisitedCountries()
    fun getCities() = countriesDao.getCities()
    fun getLocalCountriesByRange(limit: Int, offset: Int) =
        countriesDao.getCountriesByRange(limit, offset)

    fun insert(countryEntity: CountryEntity) = countriesDao.insert(countryEntity)
    fun insertAllCountries(countries: List<CountryEntity>) =
        countriesDao.insertAllCountries(countries)
    fun insertCity(cityEntity: CityEntity) = countriesDao.insertCity(cityEntity)
}