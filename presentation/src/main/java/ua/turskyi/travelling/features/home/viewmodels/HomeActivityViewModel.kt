package ua.turskyi.travelling.features.home.viewmodels

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chad.library.adapter.base.entity.node.BaseNode
import kotlinx.coroutines.launch
import ua.turskyi.domain.interactors.CountriesInteractor
import ua.turskyi.travelling.extensions.mapActualToModel
import ua.turskyi.travelling.extensions.mapModelListToActualList
import ua.turskyi.travelling.extensions.mapModelListToNodeList
import ua.turskyi.travelling.extensions.mapModelToBaseNode
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.models.VisitedCountry
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
//                TODO: What to do if notVisited count wasn't loaded?
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
                                if (country.id == city.parentId) {
                                    cityList.add(city.mapModelToBaseNode())
                                }
                            }
                        }, {
//                TODO: What to do if cities was`nt loaded?
                        })
                    }
                    country.childNode = cityList
                }
                _visitedCountriesWithCities.run { postValue(visitedCountries) }
                _visitedCountries.run { postValue(countries.mapModelListToActualList()) }
                _visibilityLoader.postValue(View.GONE)
            }, {
//                TODO: What to do if Visited countries was`nt loaded?
            })
        }
    }

    fun removeFromVisited(country: Country) {
        viewModelScope.launch {
            interactor.removeCountryModelFromVisitedList(country.mapActualToModel())
            initList()
        }
    }
}