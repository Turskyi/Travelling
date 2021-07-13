package ua.turskyi.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ua.turskyi.data.datastore.room.datasource.CountriesDbSource
import ua.turskyi.data.extensions.*
import ua.turskyi.data.network.datasource.CountriesNetSource
import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel
import ua.turskyi.domain.repository.CountriesRepository

class CountriesRepositoryImpl(private val applicationScope: CoroutineScope) : CountriesRepository,
    KoinComponent {

    private val netSource: CountriesNetSource by inject()
    private val dbSource: CountriesDbSource by inject()

    override suspend fun refreshCountries(
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = netSource.getCountryNetList({ countryNetList ->
        countryNetList?.mapNetListToModelList()?.let { modelList ->
            addModelsToDb(modelList, { onSuccess() }, { exception -> onError?.invoke(exception) })
        }
    }, { exception -> onError?.invoke(exception) })

    override suspend fun updateSelfie(
        id: Int,
        selfie: String,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        applicationScope.launch {
            dbSource.updateSelfie(id, selfie)
            onSuccess(dbSource.getVisitedLocalCountriesFromDb().mapEntityListToModelList())
        }
    }

    override suspend fun markAsVisited(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        applicationScope.launch {
            try {
                val countryLocal = country.mapModelToEntity()
                countryLocal.isVisited = true
                dbSource.insertCountry(countryLocal)
                onSuccess()
            } catch (exception: Exception) {
                onError?.invoke(exception)
            }
        }
    }

    override suspend fun removeFromVisited(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        applicationScope.launch {
            try {
                val countryLocal = country.mapModelToEntity()
                countryLocal.isVisited = false
                dbSource.removeCitiesByCountry(country.id)
                dbSource.insertCountry(countryLocal)
                onSuccess()
            } catch (exception: java.lang.Exception) {
                onError?.invoke(exception)
            }
        }
    }

    override suspend fun insertCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        applicationScope.launch {
            try {
                dbSource.insertCity(city.mapModelToEntity())
                onSuccess.invoke()
            } catch (exception: java.lang.Exception) {
                onError?.invoke(exception)
            }
        }
    }

    override suspend fun removeCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        applicationScope.launch {
            try {
                withContext(Dispatchers.Default) {
                    val cityLocal = city.mapModelToEntity()
                    dbSource.removeCity(cityLocal)
                }
                onSuccess()
            } catch (exception: java.lang.Exception) {
                onError?.invoke(exception)
            }
        }
    }

    private fun addModelsToDb(
        countries: MutableList<CountryModel>,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        applicationScope.launch {
            try {
                dbSource.insertAllCountries(countries.mapModelListToEntityList())
                onSuccess()
            } catch (exception: Exception) {
                onError?.invoke(exception)
            }
        }
    }

    override suspend fun getVisitedModelCountriesFromDb(
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        applicationScope.launch {
            onSuccess(dbSource.getVisitedLocalCountriesFromDb().mapEntityListToModelList())
        }
    }

    override suspend fun getCities(
        onSuccess: (List<CityModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        applicationScope.launch { onSuccess(dbSource.getCities().mapEntitiesToModelList()) }
    }

    override suspend fun setCountNotVisitedCountries(
        onSuccess: (Int) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        applicationScope.launch { onSuccess(dbSource.getCountNotVisitedCountries()) }
    }

    override suspend fun getCountriesByRange(
        to: Int,
        from: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        applicationScope.launch {
            onSuccess(dbSource.getLocalCountriesByRange(to, from).mapEntityListToModelList())
        }
    }

    override suspend fun loadCountriesByNameAndRange(
        name: String?,
        limit: Int,
        offset: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        applicationScope.launch {
            onSuccess(
                dbSource.loadCountriesByNameAndRange(name, limit, offset).mapEntityListToModelList()
            )
        }
    }
}