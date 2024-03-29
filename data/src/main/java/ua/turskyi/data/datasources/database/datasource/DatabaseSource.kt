package ua.turskyi.data.datasources.database.datasource

import ua.turskyi.data.datasources.database.dao.CountriesDao
import ua.turskyi.data.entities.local.CityEntity
import ua.turskyi.data.entities.local.CountryEntity

class DatabaseSource(private val countriesDao: CountriesDao) {
    fun loadCountriesByNameAndRange(
        name: String,
        limit: Int,
        offset: Int
    ): MutableList<CountryEntity> {
        return countriesDao.loadAllCountriesByNameAndRange(name, limit, offset)
    }

    fun getLocalCountriesByRange(limit: Int, offset: Int): List<CountryEntity> {
        return countriesDao.getCountriesByRange(limit, offset)
    }

    fun getCountNotVisitedCountries(): Int = countriesDao.getNumNotVisitedCountries()
    fun getVisitedLocalCountriesFromDb() = countriesDao.getVisitedCountries()
    fun getCities(): List<CityEntity> = countriesDao.getCities()
    fun insertCountry(countryEntity: CountryEntity) = countriesDao.insertCountry(countryEntity)

    fun insertAllCountries(countries: List<CountryEntity>) {
        countriesDao.insertAllCountries(countries)
    }

    fun insertCity(cityEntity: CityEntity) = countriesDao.insertCity(cityEntity)
    fun removeCity(city: CityEntity) = countriesDao.delete(city)
    fun removeCitiesByCountry(parentId: Int) = countriesDao.removeCitiesByCountry(parentId)
    fun updateSelfie(id: Int, filePath: String) = countriesDao.updateSelfie(id, filePath)
}