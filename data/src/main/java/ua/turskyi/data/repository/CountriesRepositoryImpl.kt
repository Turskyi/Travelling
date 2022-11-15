package ua.turskyi.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ua.turskyi.data.database.room.datasource.DatabaseSource
import ua.turskyi.data.entities.local.CityEntity
import ua.turskyi.data.entities.local.CountryEntity
import ua.turskyi.data.extensions.*
import ua.turskyi.data.network.datasource.NetSource
import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel
import ua.turskyi.domain.repository.CountriesRepository

class CountriesRepositoryImpl(private val applicationScope: CoroutineScope) : CountriesRepository,
    KoinComponent {

    private val netSource: NetSource by inject()
    private val databaseSource: DatabaseSource by inject()

    override suspend fun refreshCountries(
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        netSource.getCountryNetList(
            onComplete = { countryNetList ->
                countryNetList?.mapNetListToModelList()?.let { modelList ->
                    addModelsToDb(
                        modelList,
                        { onSuccess() },
                        { exception -> onError.invoke(exception) })
                }
            },
            onError = { exception -> onError.invoke(exception) },
        )
    }

    override suspend fun updateSelfie(
        id: Int,
        filePath: String,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        applicationScope.launch {
            databaseSource.updateSelfie(id, filePath)
            onSuccess(databaseSource.getVisitedLocalCountriesFromDb().mapEntityListToModelList())
        }
    }

    override suspend fun markAsVisited(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        applicationScope.launch {
            try {
                val countryLocal: CountryEntity = country.mapModelToEntity()
                countryLocal.isVisited = true
                databaseSource.insertCountry(countryLocal)
                onSuccess()
            } catch (exception: Exception) {
                onError.invoke(exception)
            }
        }
    }

    override suspend fun removeFromVisited(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        applicationScope.launch {
            try {
                val countryLocal: CountryEntity = country.mapModelToEntity()
                countryLocal.isVisited = false
                databaseSource.removeCitiesByCountry(country.id)
                databaseSource.insertCountry(countryLocal)
                onSuccess()
            } catch (exception: java.lang.Exception) {
                onError.invoke(exception)
            }
        }
    }

    override suspend fun insertCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        applicationScope.launch {
            try {
                databaseSource.insertCity(city.mapModelToEntity())
                onSuccess.invoke()
            } catch (exception: java.lang.Exception) {
                onError.invoke(exception)
            }
        }
    }

    override suspend fun removeCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        applicationScope.launch {
            try {
                withContext(Dispatchers.Default) {
                    val cityLocal: CityEntity = city.mapModelToEntity()
                    databaseSource.removeCity(cityLocal)
                }
                onSuccess()
            } catch (exception: java.lang.Exception) {
                onError.invoke(exception)
            }
        }
    }

    private fun addModelsToDb(
        countries: MutableList<CountryModel>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        applicationScope.launch {
            try {
                databaseSource.insertAllCountries(countries.mapModelListToEntityList())
                onSuccess()
            } catch (exception: Exception) {
                onError.invoke(exception)
            }
        }
    }

    override suspend fun setVisitedModelCountriesFromDb(
        onSuccess: (List<CountryModel>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        applicationScope.launch {
            onSuccess(databaseSource.getVisitedLocalCountriesFromDb().mapEntityListToModelList())
        }
    }

    override suspend fun setCities(
        onSuccess: (List<CityModel>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        applicationScope.launch { onSuccess(databaseSource.getCities().mapEntitiesToModelList()) }
    }

    override suspend fun setCountNotVisitedCountries(
        onSuccess: (Int) -> Unit,
        onError: (Exception) -> Unit
    ) {
        applicationScope.launch { onSuccess(databaseSource.getCountNotVisitedCountries()) }
    }

    override suspend fun setCountriesByRange(
        to: Int,
        from: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        applicationScope.launch {
            onSuccess(databaseSource.getLocalCountriesByRange(to, from).mapEntityListToModelList())
        }
    }

    override suspend fun loadCountriesByNameAndRange(
        name: String,
        limit: Int,
        offset: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        applicationScope.launch {
            onSuccess(
                databaseSource.loadCountriesByNameAndRange(name, limit, offset)
                    .mapEntityListToModelList()
            )
        }
    }
}