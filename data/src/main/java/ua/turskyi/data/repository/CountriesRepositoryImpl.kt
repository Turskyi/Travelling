package ua.turskyi.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ua.turskyi.data.datasources.database.datasource.DatabaseSource
import ua.turskyi.data.datasources.webservice.NetSource
import ua.turskyi.data.entities.local.CountryEntity
import ua.turskyi.data.entities.network.CountryResponse
import ua.turskyi.data.extensions.mapEntitiesToModelList
import ua.turskyi.data.extensions.mapEntityListToModelList
import ua.turskyi.data.extensions.mapModelListToEntityList
import ua.turskyi.data.extensions.mapModelToEntity
import ua.turskyi.data.extensions.mapNetListToModelList
import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel
import ua.turskyi.domain.repository.CountriesRepository

class CountriesRepositoryImpl : CountriesRepository,
    KoinComponent {

    private val netSource: NetSource by inject()
    private val databaseSource: DatabaseSource by inject()

    override suspend fun refreshCountries(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            var result: Result<Unit> = Result.failure(Exception("Unknown error"))
            netSource.getCountryNetList(
                onComplete = { countryNetList: List<CountryResponse>? ->
                    val modelList: MutableList<CountryModel>? =
                        countryNetList?.mapNetListToModelList()
                    if (modelList != null) {
                        databaseSource.insertAllCountries(modelList.mapModelListToEntityList())
                        result = Result.success(Unit)
                    }
                },
                onError = { exception: Exception ->
                    result = Result.failure(exception)
                },
            )
            result
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun updateSelfie(id: Int, filePath: String): Result<List<CountryModel>> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                databaseSource.updateSelfie(id, filePath)
                Result.success(databaseSource.getVisitedLocalCountriesFromDb().mapEntityListToModelList())
            } catch (exception: Exception) {
                Result.failure(exception)
            }
        }

    override suspend fun markAsVisited(country: CountryModel): Result<Unit> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val countryLocal: CountryEntity = country.mapModelToEntity()
                countryLocal.isVisited = true
                databaseSource.insertCountry(countryLocal)
                Result.success(Unit)
            } catch (exception: Exception) {
                Result.failure(exception)
            }
        }

    override suspend fun removeFromVisited(country: CountryModel): Result<Unit> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val countryLocal: CountryEntity = country.mapModelToEntity()
                countryLocal.isVisited = false
                databaseSource.removeCitiesByCountry(country.id)
                databaseSource.insertCountry(countryLocal)
                Result.success(Unit)
            } catch (exception: Exception) {
                Result.failure(exception)
            }
        }

    override suspend fun insertCity(city: CityModel): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            databaseSource.insertCity(city.mapModelToEntity())
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun removeCity(city: CityModel): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val cityLocal: ua.turskyi.data.entities.local.CityEntity = city.mapModelToEntity()
            databaseSource.removeCity(cityLocal)
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun setVisitedModelCountriesFromDb(): Result<List<CountryModel>> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                Result.success(databaseSource.getVisitedLocalCountriesFromDb().mapEntityListToModelList())
            } catch (exception: Exception) {
                Result.failure(exception)
            }
        }

    override suspend fun setCities(): Result<List<CityModel>> = withContext(Dispatchers.IO) {
        return@withContext try {
            Result.success(databaseSource.getCities().mapEntitiesToModelList())
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun setCountNotVisitedCountries(): Result<Int> = withContext(Dispatchers.IO) {
        return@withContext try {
            Result.success(databaseSource.getCountNotVisitedCountries())
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun setCountriesByRange(limit: Int, offset: Int): Result<List<CountryModel>> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                Result.success(databaseSource.getLocalCountriesByRange(limit, offset).mapEntityListToModelList())
            } catch (exception: Exception) {
                Result.failure(exception)
            }
        }

    override suspend fun loadCountriesByNameAndRange(
        name: String,
        limit: Int,
        offset: Int
    ): Result<List<CountryModel>> = withContext(Dispatchers.IO) {
        return@withContext try {
            Result.success(
                databaseSource.loadCountriesByNameAndRange(name, limit, offset)
                    .mapEntityListToModelList()
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
