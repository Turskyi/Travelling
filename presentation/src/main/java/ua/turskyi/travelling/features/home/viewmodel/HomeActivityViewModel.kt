package ua.turskyi.travelling.features.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ua.turskyi.domain.interactors.CountriesInteractor
import ua.turskyi.travelling.extensions.mapActualToModel
import ua.turskyi.travelling.extensions.mapModelListToActualList
import ua.turskyi.travelling.model.Country
import ua.turskyi.travelling.utils.isOnline

class HomeActivityViewModel(private val interactor: CountriesInteractor) :
    ViewModel(){

    var notVisitedCount = 0

    private val _visitedCountries = MutableLiveData<List<Country>>()
    var visitedCountries: LiveData<List<Country>>

    private val _navigateToAllCountries = MutableLiveData<Boolean>()
    val navigateToAllCountries: LiveData<Boolean>
        get() = _navigateToAllCountries

    private val _countries = MutableLiveData<List<Country>>()
    var countries: LiveData<List<Country>>

    init {
        visitedCountries = _visitedCountries
        countries = _countries
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
                _visitedCountries.run { postValue(countries.mapModelListToActualList()) }
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