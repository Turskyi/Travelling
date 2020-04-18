package ua.turskyi.data.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import ua.turskyi.data.api.datasource.CountriesNetSource
import ua.turskyi.data.extensions.*
import ua.turskyi.data.room.datasource.CountriesDbSource
import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel
import ua.turskyi.domain.repository.CountriesRepository

class CountriesRepositoryImpl : CountriesRepository, KoinComponent {

    companion object {
        const val CITY_LOG = "CITY_LOG"
    }
    private val countriesNetSource: CountriesNetSource by inject()
    private val countriesDbSource: CountriesDbSource by inject()

    override suspend fun refreshCountriesInDb(
        onSusses: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        countriesNetSource.getCountryNetList({ countryNetList ->
            GlobalScope.launch {
                countryNetList?.mapNetListToModelList()?.let { modelList ->
                    addModelsToDb(modelList)
                }
                withContext(Dispatchers.Main) {
                    onSusses()
                }
            }
        }, {
            onError?.invoke(it)
        })
    }

    override suspend fun markAsVisited(
        country: CountryModel,
        onError: ((Exception) -> Unit?)?
    ) {
        GlobalScope.launch {
            val countryLocal = country.mapModelToEntity()
            countryLocal.visited = true
            countriesDbSource.insert(countryLocal)
        }
    }

    override suspend fun removeFromVisited(
        country: CountryModel,
        onError: ((Exception) -> Unit?)?
    ) {
        GlobalScope.launch {
            try {
                val countryLocal = country.mapModelToEntity()
                countryLocal.visited = false
                countriesDbSource.insert(countryLocal)
            } catch (e: java.lang.Exception) {
                onError?.invoke(e)
            }
        }
    }

    override suspend fun getCountriesByRange(
        limit: Int,
        offset: Int,
        onSusses: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        GlobalScope.launch {
            onSusses(
                countriesDbSource.getLocalCountriesByRange(limit, offset)
                    .mapEntityListToModelList()
            )
        }
    }

    override suspend fun addModelsToDb(
        countries: MutableList<CountryModel>,
        onError: ((Exception) -> Unit?)?
    ){
        try {
            countriesDbSource.insertAllCountries(countries.mapModelListToEntityList())
        } catch (e: Exception) {
            onError?.invoke(e)
        }
    }

    override suspend fun getVisitedModelCountriesFromDb(
        onSusses: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        GlobalScope.launch {
            onSusses(
                countriesDbSource.getVisitedLocalCountriesFromDb()
                    .mapEntityListToModelList()
            )
        }
    }

    override suspend fun getCities(
        onSusses: (MutableList<CityModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        GlobalScope.launch {
            onSusses(
                countriesDbSource.getCities().mapEntitiesToModelList()
            )
        }
    }

    override suspend fun getNumNotVisitedCountries(
        onSusses: (Int) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        GlobalScope.launch {
            onSusses(countriesDbSource.getNumNotVisitedCountries())
        }
    }

    override suspend fun insertCity(
       city: CityModel,
        onError: ((Exception) -> Unit?)?
    ) {
        GlobalScope.launch {
            countriesDbSource.insertCity(city.mapModelToEntity())
        }
    }

    override suspend fun addCityToCountry(
        country: CountryModel,
        onError: ((Exception) -> Unit?)?
    ) {
        GlobalScope.launch {
            try {
                val countryLocal = country.mapModelToEntity()
                Log.d(CITY_LOG, "data.repositoryImpl: ${countryLocal.cities}")
                countriesDbSource.insert(countryLocal)
            } catch (e: java.lang.Exception) {
                onError?.invoke(e)
            }
        }
    }
}