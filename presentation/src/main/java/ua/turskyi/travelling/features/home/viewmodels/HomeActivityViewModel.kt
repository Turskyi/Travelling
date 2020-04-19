package ua.turskyi.travelling.features.home.viewmodels

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chad.library.adapter.base.entity.node.BaseNode
import kotlinx.coroutines.launch
import okhttp3.Interceptor.Companion.invoke
import ua.turskyi.domain.interactors.CountriesInteractor
import ua.turskyi.travelling.extensions.*
import ua.turskyi.travelling.features.home.view.ui.AddCityDialogFragment.Companion.CITY_LOG
import ua.turskyi.travelling.models.City
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.models.VisitedCountry
import ua.turskyi.travelling.utils.Tips
import ua.turskyi.travelling.utils.isOnline

class HomeActivityViewModel(private val interactor: CountriesInteractor) : ViewModel(){

    var notVisitedCount = 0

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
        _visibilityLoader.postValue(View.VISIBLE)
        visitedCountries = _visitedCountries
        visitedCountriesWithCities = _visitedCountriesWithCities
    }

    suspend fun initList() {
        val load: () -> Unit = {
            viewModelScope.launch {
                interactor.getNotVisitedCountriesNum({ notVisitedCountriesNum ->
                    getVisitedCountriesFromDB()
                    notVisitedCount = notVisitedCountriesNum
                }, {
                    getVisitedCountriesFromDB()
                })
            }
        }
        when {
            isOnline() -> interactor.refreshCountries({ load() }, { load() })
            else -> load()
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
                                Log.d(CITY_LOG, "init ${city.id} ${city.name} ${city.parentId}")
                                if (country.id == city.parentId) {
                                    cityList.add(city.mapModelToBaseNode())
                                }
                            }
                        }, {
                            Tips.show("OOPS! COULDN'T LOAD CITIES")
                        })
                    }
                    country.childNode = cityList
                }
                _visitedCountriesWithCities.run { postValue(visitedCountries) }
                _visitedCountries.run { postValue(countries.mapModelListToActualList()) }
                _visibilityLoader.postValue(View.GONE)
            }, {
                Tips.show("OOPS! COULDN'T LOAD VISITED COUNTRIES")
            })
        }
    }

    fun removeFromVisited(country: Country) {
        viewModelScope.launch {
            interactor.removeCountryModelFromVisitedList(country.mapActualToModel())
            initList()
        }
    }

    fun removeCity(city: City) {
        viewModelScope.launch {
            Log.d(CITY_LOG, "remove ${city.id} ${city.name} ${city.parentId}")
            interactor.removeCity(city.mapNodeToModel())
            initList()
        }
    }
}