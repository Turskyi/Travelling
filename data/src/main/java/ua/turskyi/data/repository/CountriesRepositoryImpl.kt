package ua.turskyi.data.repository

import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import ua.turskyi.data.Prefs
import ua.turskyi.data.network.datasource.CountriesNetSource
import ua.turskyi.data.extensions.*
import ua.turskyi.data.firestore.FirestoreSource
import ua.turskyi.data.database.datasource.CountriesDbSource
import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel
import ua.turskyi.domain.repository.CountriesRepository

class CountriesRepositoryImpl(private val applicationScope: CoroutineScope) : CountriesRepository, KoinComponent {

    private val netSource: CountriesNetSource by inject()
    private val dbSource: CountriesDbSource by inject()
    private val firebaseSource: FirestoreSource by inject()
    private val prefs: Prefs by inject()

    override var isSynchronized: Boolean
        get() = prefs.isSynchronized
        set(isSynchronized) {
            prefs.isSynchronized = isSynchronized
        }

    override var isUpgraded: Boolean
        get() = prefs.isUpgraded
        set(isUpgraded) {
            prefs.isUpgraded = isUpgraded
        }

    override suspend fun refreshCountries(
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = netSource.getCountryNetList({ countryNetList ->
        countryNetList?.mapNetListToModelList()?.let { modelList ->
            addModelsToDb(modelList, { onSuccess() }, { exception -> onError?.invoke(exception) })
        }
    }, { exception -> onError?.invoke(exception) })

    override suspend fun syncVisitedCountries(
        onSuccess: (Job?) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        onSuccess(GlobalScope.launch {
            refreshCountries({
                dbSource.getVisitedLocalCountriesFromDb().forEach { country ->
                    GlobalScope.launch {
                        markAsVisited(country.mapEntityToModel(), {
//                            implement check if last then call success in feature release
                        }, { exception ->
                            onError?.invoke(exception)
                        })
                    }
                }
                dbSource.getCities().forEach { city ->
                    applicationScope.launch {
                        insertCity(city.mapEntityToModel(),{
//                            implement check if last then call success in feature release
                        }, { exception ->
                            onError?.invoke(exception)
                        })
                    }
                }
            }, { exception ->
                onError?.invoke(exception)
            })
        })

    }

    override suspend fun updateSelfie(
        id: Int,
        selfie: String,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        if (prefs.isSynchronized) {
            firebaseSource.updateSelfie(id.toString(), selfie)
        } else {
            applicationScope.launch {
                dbSource.updateSelfie(id, selfie)
                onSuccess(dbSource.getVisitedLocalCountriesFromDb().mapEntityListToModelList())
            }
        }
    }

    override suspend fun markAsVisited(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        if (prefs.isUpgraded) {
            firebaseSource.markAsVisited(
                country.mapModelToEntity(), { onSuccess() },
                { exception -> onError?.invoke(exception) })
        } else {
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
    }

    override suspend fun removeFromVisited(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        if (prefs.isSynchronized) {
            firebaseSource.removeFromVisited(country.name, country.id, { onSuccess() },
                { exception -> onError?.invoke(exception) })
        } else {
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
    }

    override suspend fun insertCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        if (prefs.isUpgraded) {
            firebaseSource.insertCity(city.mapModelToEntity(), { onSuccess() },
                { exception -> onError?.invoke(exception) })
        } else {
            applicationScope.launch {
                try {
                    dbSource.insertCity(city.mapModelToEntity())
                    onSuccess()
                } catch (exception: java.lang.Exception) {
                    onError?.invoke(exception)
                }
            }
        }
    }

    override suspend fun removeCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        if (prefs.isSynchronized) {
            firebaseSource.removeCity(city.name, { onSuccess() },
                { exception -> onError?.invoke(exception) })
        } else {
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
    }

    private fun addModelsToDb(
        countries: MutableList<CountryModel>,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        if (prefs.isUpgraded) {
            firebaseSource.insertAllCountries(
                countries.mapModelListToEntityList(), { onSuccess() },
                { exception -> onError?.invoke(exception) })
        } else {
            applicationScope.launch {
                try {
                    dbSource.insertAllCountries(countries.mapModelListToEntityList())
                    onSuccess()
                } catch (exception: Exception) {
                    onError?.invoke(exception)
                }
            }
        }
    }

    override suspend fun getVisitedModelCountriesFromDb(
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        if (prefs.isSynchronized) {
            firebaseSource.getVisitedCountries({ countries ->
                onSuccess(countries)
            }, { exception ->
                onError?.invoke(exception)
            })
        } else {
            applicationScope.launch {
                onSuccess(
                    dbSource.getVisitedLocalCountriesFromDb()
                        .mapEntityListToModelList()
                )
            }
        }
    }

    override suspend fun getCities(
        onSuccess: (List<CityModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        if (prefs.isSynchronized) {
            firebaseSource.getCities({ cities ->
                onSuccess(cities)
            }, { exception ->
                onError?.invoke(exception)
            })
        } else {
            applicationScope.launch {
                onSuccess(
                    dbSource.getCities().mapEntitiesToModelList()
                )
            }
        }
    }

    override suspend fun getCountNotVisitedCountries(
        onSuccess: (Int) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        if (prefs.isSynchronized) {
            firebaseSource.getCountNotVisitedCountries({ count -> onSuccess(count) },
                { onError?.invoke(it) })
        } else {
            applicationScope.launch {
                onSuccess(dbSource.getCountNotVisitedCountries())
            }
        }
    }

    override suspend fun getCountriesByRange(
        to: Int,
        from: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        if (prefs.isSynchronized) {
            firebaseSource.getCountriesByRange(to, from, { list -> onSuccess(list) },
                { onError?.invoke(it) })
        } else {
            applicationScope.launch {
                onSuccess(
                    dbSource.getLocalCountriesByRange(to, from)
                        .mapEntityListToModelList()
                )
            }
        }
    }

    override suspend fun loadCountriesByNameAndRange(
        name: String?,
        limit: Int,
        offset: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        if (prefs.isSynchronized) {
            firebaseSource.getCountriesByNameAndRange(name,
                limit,
                offset,
                { list -> onSuccess(list) },
                { onError?.invoke(it) })
        } else {
            applicationScope.launch {
                onSuccess(
                    dbSource.loadCountriesByNameAndRange(name, limit, offset)
                        .mapEntityListToModelList()
                )
            }
        }
    }
}