package ua.turskyi.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import ua.turskyi.data.api.datasource.CountriesNetSource
import ua.turskyi.data.extensions.mapEntityListToModelList
import ua.turskyi.data.extensions.mapModelListToEntityList
import ua.turskyi.data.extensions.mapModelToEntity
import ua.turskyi.data.extensions.mapNetListToModelList
import ua.turskyi.data.room.datasource.CountriesDbSource
import ua.turskyi.domain.model.CountryModel
import ua.turskyi.domain.repository.CountriesRepository

class CountriesRepositoryImpl : CountriesRepository, KoinComponent {
    private val countriesNetSource: CountriesNetSource by inject()
    private val countriesDbSource: CountriesDbSource by inject()

    override suspend fun refreshCountriesInDb(
        onSusses: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        countriesNetSource.getCountryNetList({ countryNetList ->
            GlobalScope.launch {
                countryNetList?.mapNetListToModelList()?.let { modelList ->
                    addModelToDb(modelList)
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

    override suspend fun addModelToDb(
        countries: List<CountryModel>,
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

    override suspend fun getNumNotVisitedCountries(
        onSusses: (Int) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        GlobalScope.launch {
            onSusses(countriesDbSource.getNumNotVisitedCountries())
        }
    }

    override suspend fun getModelCountriesFromDb(
        onSusses: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        onSusses(countriesDbSource.getAllLocalCountries().mapEntityListToModelList())
    }
}