package ua.turskyi.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import ua.turskyi.data.Prefs
import ua.turskyi.data.api.datasource.CountriesNetSource
import ua.turskyi.data.extensions.*
import ua.turskyi.data.firestoreSource.FirebaseSource
import ua.turskyi.data.room.datasource.CountriesDbSource
import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel
import ua.turskyi.domain.repository.CountriesRepository

class CountriesRepositoryImpl : CountriesRepository, KoinComponent {

    private val netSource: CountriesNetSource by inject()
    private val dbSource: CountriesDbSource by inject()
    private val firebaseSource: FirebaseSource by inject()
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

    override suspend fun refreshCountriesInDb(
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        try {
            if (!isSynchronized) {
                netSource.getCountryNetList({ countryNetList ->
                    GlobalScope.launch {
                        countryNetList?.mapNetListToModelList()?.let { modelList ->
                            addModelsToDb(modelList) {
                                onError?.invoke(it)
                            }
                        }
                        withContext(Dispatchers.Main) {
                            onSuccess()
                        }
                    }
                }, {
                    onError?.invoke(it)
                })
            }
        } catch (e: Exception) {
            onError?.invoke(e)
        }
    }

    override suspend fun syncVisitedCountries(
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        GlobalScope.launch {
            refreshCountriesInDb({
                dbSource.getVisitedLocalCountriesFromDb().forEach { country ->
                    GlobalScope.launch {
                        markAsVisited(country.mapEntityToModel()) {
                            onError?.invoke(it)
                        }
                    }
                }
                dbSource.getCities().forEach { city ->
                    GlobalScope.launch {
                        insertCity(city.mapEntityToModel()) {
                            onError?.invoke(it)
                        }
                    }
                }
                onSuccess()
            }, {
                onError?.invoke(it)
            })
        }
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
            GlobalScope.launch {
                dbSource.updateSelfie(id, selfie)
                onSuccess(dbSource.getVisitedLocalCountriesFromDb().mapEntityListToModelList())
            }
        }
    }

    override suspend fun markAsVisited(
        country: CountryModel,
        onError: ((Exception) -> Unit?)?
    ) {
        if (prefs.isUpgraded) {
            firebaseSource.markAsVisited(country.id.toString())
        } else {
            GlobalScope.launch {
                val countryLocal = country.mapModelToEntity()
                countryLocal.visited = true
                dbSource.insertCountry(countryLocal)
            }
        }
    }

    override suspend fun removeFromVisited(
        country: CountryModel,
        onError: ((Exception) -> Unit?)?
    ) {
        if (prefs.isSynchronized) {
            firebaseSource.removeFromVisited(country.id.toString())
        } else {
            GlobalScope.launch {
                try {
                    val countryLocal = country.mapModelToEntity()
                    countryLocal.visited = false
                    dbSource.removeCitiesByCountry(country.id)
                    dbSource.insertCountry(countryLocal)
                } catch (e: java.lang.Exception) {
                    onError?.invoke(e)
                }
            }
        }
    }

    override suspend fun insertCity(
        city: CityModel,
        onError: ((Exception) -> Unit?)?
    ) {
        if (prefs.isUpgraded) {
            firebaseSource.insertCity(city)
        } else {
            GlobalScope.launch {
                dbSource.insertCity(city.mapModelToEntity())
            }
        }
    }

    override suspend fun removeCity(
        city: CityModel,
        onError: ((Exception) -> Unit?)?
    ) {
        if (prefs.isSynchronized) {
            firebaseSource.removeCity(city.id.toString())
        } else {
            GlobalScope.launch {
                try {
                    val cityLocal = city.mapModelToEntity()
                    dbSource.removeCity(cityLocal)
                } catch (e: java.lang.Exception) {
                    onError?.invoke(e)
                }
            }
        }
    }

    private fun addModelsToDb(
        countries: MutableList<CountryModel>,
        onError: ((Exception) -> Unit?)?
    ) {
        try {
            if (prefs.isUpgraded) {
                firebaseSource.insertAllCountries(countries)
            } else {
                dbSource.insertAllCountries(countries.mapModelListToEntityList())
            }
        } catch (e: Exception) {
            onError?.invoke(e)
        }
    }

    override suspend fun getVisitedModelCountriesFromDb(
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        if (prefs.isSynchronized) {
            firebaseSource.getVisitedCountries({ countries ->
                onSuccess(countries)
            }, {
                onError?.invoke(it)
            })
        } else {
            GlobalScope.launch {
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
            }, {
                onError?.invoke(it)
            })
        } else {
            GlobalScope.launch {
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
            GlobalScope.launch {
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
            GlobalScope.launch {
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
            GlobalScope.launch {
                onSuccess(
                    dbSource.loadCountriesByNameAndRange(name, limit, offset)
                        .mapEntityListToModelList()
                )
            }
        }
    }
}