package ua.turskyi.data.repository

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

    private val countriesNetSource: CountriesNetSource by inject()
    private val countriesDbSource: CountriesDbSource by inject()

    override suspend fun refreshCountriesInDb(
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        countriesNetSource.getCountryNetList({ countryNetList ->
            GlobalScope.launch {
                countryNetList?.mapNetListToModelList()?.let { modelList ->
                    addModelsToDb(modelList)
                }
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            }
        }, {
            onError?.invoke(it)
        })
    }

    override suspend fun updateSelfie(
        id: Int,
        selfie: String,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        GlobalScope.launch {
            countriesDbSource.updateSelfie(id, selfie)
            onSuccess(countriesDbSource.getVisitedLocalCountriesFromDb().mapEntityListToModelList())
        }
    }

    override suspend fun markAsVisited(
        country: CountryModel,
        onError: ((Exception) -> Unit?)?
    ) {
        GlobalScope.launch {
            val countryLocal = country.mapModelToEntity()
            countryLocal.visited = true
            countriesDbSource.insertCountry(countryLocal)
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
                countriesDbSource.removeCitiesByCountry(country.id)
                countriesDbSource.insertCountry(countryLocal)
            } catch (e: java.lang.Exception) {
                onError?.invoke(e)
            }
        }
    }

    override suspend fun removeCity(
        city: CityModel,
        onError: ((Exception) -> Unit?)?
    ) {
        GlobalScope.launch {
            try {
                val cityLocal = city.mapModelToEntity()
                countriesDbSource.removeCity(cityLocal)
            } catch (e: java.lang.Exception) {
                onError?.invoke(e)
            }
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
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        GlobalScope.launch {
            onSuccess(
                countriesDbSource.getVisitedLocalCountriesFromDb()
                    .mapEntityListToModelList()
            )
        }
    }

    override suspend fun getCities(
        onSuccess: (MutableList<CityModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        GlobalScope.launch {
            onSuccess(
                countriesDbSource.getCities().mapEntitiesToModelList()
            )
        }
    }

    override suspend fun getNumNotVisitedCountries(
        onSuccess: (Int) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        GlobalScope.launch {
            onSuccess(countriesDbSource.getNumNotVisitedCountries())
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

    override suspend fun getCountriesByRange(
        limit: Int,
        offset: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        GlobalScope.launch {
            onSuccess(
                countriesDbSource.getLocalCountriesByRange(limit, offset)
                    .mapEntityListToModelList()
            )
        }
    }

    override suspend fun loadCountriesByNameAndRange(
        name: String?,
        limit: Int,
        offset: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        GlobalScope.launch {
            onSuccess(
                countriesDbSource.loadCountriesByNameAndRange(name,limit, offset)
                    .mapEntityListToModelList()
            )
        }
    }
}