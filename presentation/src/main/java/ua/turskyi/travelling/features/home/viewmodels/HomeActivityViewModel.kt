package ua.turskyi.travelling.features.home.viewmodels

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chad.library.adapter.base.entity.node.BaseNode
import kotlinx.coroutines.launch
import ua.turskyi.domain.interactor.CountriesInteractor
import ua.turskyi.travelling.common.prefs
import ua.turskyi.travelling.extensions.*
import ua.turskyi.travelling.models.City
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.models.VisitedCountry
import ua.turskyi.travelling.utils.isOnline

class HomeActivityViewModel(private val interactor: CountriesInteractor) : ViewModel() {

    var notVisitedCount: Float = 0F
    var citiesCount = 0

    private val _visibilityLoader = MutableLiveData<Int>()
    val visibilityLoader: MutableLiveData<Int>
        get() = _visibilityLoader

    private val _visitedCountries = MutableLiveData<List<Country>>()
    var visitedCountries: LiveData<List<Country>>

    private val _visitedCountriesWithCities = MutableLiveData<List<VisitedCountry>>()
    var visitedCountriesWithCities: LiveData<List<VisitedCountry>>

    private val _navigateToAllCountries = MutableLiveData<Boolean>()
    val navigateToAllCountries: LiveData<Boolean>
        get() = _navigateToAllCountries

    init {
        _visibilityLoader.postValue(VISIBLE)
        visitedCountries = _visitedCountries
        visitedCountriesWithCities = _visitedCountriesWithCities
    }

    suspend fun initListOfCountries() {
        val loadCountries: () -> Unit = {
            viewModelScope.launch {
                interactor.getNotVisitedCountriesNum({ notVisitedCountriesNum ->
                    getVisitedCountriesFromDB()
                    notVisitedCount = notVisitedCountriesNum.toFloat()
                }, {
                    getVisitedCountriesFromDB()
                    it.printStackTrace()
                })
            }
        }
        when {
            isOnline() -> interactor.refreshCountries({ loadCountries() }, { loadCountries() })
            else -> loadCountries()
        }
    }

    fun onFloatBtnClicked() {
        _navigateToAllCountries.value = true
    }

    fun onNavigatedToAllCountries() {
        _navigateToAllCountries.value = false
    }

    private fun getVisitedCountriesFromDB() {
        viewModelScope.launch {
            interactor.getVisitedModelCountries({ countries ->
                val visitedCountries = countries.mapModelListToNodeList()
                for (country in visitedCountries) {
                    val cityList = mutableListOf<BaseNode>()
                    viewModelScope.launch {
                        interactor.getCities({ cities ->
                            for (city in cities) {
                                if (country.id == city.parentId) {
                                    cityList.add(city.mapModelToBaseNode())
                                }
                            }
                            citiesCount = cities.size
                        }, {
                            it.printStackTrace()
                        })
                    }
                    country.childNode = cityList
                }
                _visitedCountriesWithCities.run { postValue(visitedCountries) }
                _visitedCountries.run { postValue(countries.mapModelListToActualList()) }
                _visibilityLoader.postValue(GONE)
            }, {
                it.printStackTrace()
            })
        }
    }

    fun removeFromVisited(country: Country) {
        viewModelScope.launch {
            interactor.removeCountryModelFromVisitedList(country.mapActualToModel())
            initListOfCountries()
        }
    }

    fun removeCity(city: City) {
        viewModelScope.launch {
            interactor.removeCity(city.mapNodeToModel())
            initListOfCountries()
        }
    }

    fun syncDatabaseWithFireStore() {
        _visibilityLoader.postValue(VISIBLE)
//                TODO: sync database with firestore
        prefs.isSynchronized = true
        _visibilityLoader.postValue(GONE)
    }
}