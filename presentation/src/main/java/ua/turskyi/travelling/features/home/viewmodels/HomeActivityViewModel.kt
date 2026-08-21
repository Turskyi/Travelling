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
import ua.turskyi.domain.model.CountryModel
import ua.turskyi.travelling.models.City
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.models.VisitedCountry
import ua.turskyi.travelling.utils.Event
import ua.turskyi.travelling.utils.extensions.mapModelListToCountryList
import ua.turskyi.travelling.utils.extensions.mapModelListToNodeList
import ua.turskyi.travelling.utils.extensions.mapModelToBaseNode
import ua.turskyi.travelling.utils.extensions.mapNodeToModel
import ua.turskyi.travelling.utils.extensions.mapToModel

class HomeActivityViewModel(private val interactor: CountriesInteractor) : ViewModel() {

    var notVisitedCountriesCount: Float = 0F
    var citiesCount = 0
    var isPermissionGranted: Boolean = false
    var isDoubleBackToExitPressed = false
    var mLastClickTime: Long = 0

    private val _visibilityLoader = MutableLiveData<Int>()
    val visibilityLoader: LiveData<Int>
        get() = _visibilityLoader

    private val _visitedCountries = MutableLiveData<List<Country>>()
    val visitedCountries: LiveData<List<Country>>
        get() = _visitedCountries

    private val _visitedCountriesWithCities = MutableLiveData<List<VisitedCountry>>()
    val visitedCountriesWithCities: LiveData<List<VisitedCountry>>
        get() = _visitedCountriesWithCities

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>>
        get() = _errorMessage

    private val _navigateToAllCountries = MutableLiveData<Boolean>()
    val navigateToAllCountries: LiveData<Boolean>
        get() = _navigateToAllCountries

    fun showListOfVisitedCountries() {
        _visibilityLoader.postValue(VISIBLE)
        viewModelScope.launch {
            interactor.setNotVisitedCountriesNum().onSuccess { notVisitedCountriesNum ->
                notVisitedCountriesCount = notVisitedCountriesNum.toFloat()
                setVisitedCountries(notVisitedCountriesNum)
            }.onFailure { exception ->
                _errorMessage.postValue(
                    Event(exception.localizedMessage ?: exception.toString()),
                )
            }
        }
    }

    private fun setVisitedCountries(notVisitedCountriesNum: Int) {
        viewModelScope.launch {
            interactor.setVisitedCountries().onSuccess { visitedCountries ->
                if (notVisitedCountriesNum == 0 && visitedCountries.isEmpty()) {
                    downloadCountries()
                } else {
                    val visitedNodeCountries: MutableList<VisitedCountry> =
                        visitedCountries.mapModelListToNodeList()
                    if (visitedNodeCountries.isEmpty()) {
                        _visitedCountriesWithCities.postValue(visitedNodeCountries)
                        _visitedCountries.postValue(visitedCountries.mapModelListToCountryList())
                        _visibilityLoader.postValue(GONE)
                    } else {
                        addCitiesToVisitedCountries(visitedNodeCountries, visitedCountries)
                    }
                }
            }.onFailure { exception ->
                _visibilityLoader.postValue(GONE)
                _errorMessage.postValue(
                    Event(exception.localizedMessage ?: exception.toString()),
                )
            }
        }
    }

    private fun addCitiesToVisitedCountries(
        visitedNodeCountries: MutableList<VisitedCountry>,
        visitedCountries: List<CountryModel>
    ) {
        viewModelScope.launch {
            interactor.setCities().onSuccess { cities ->
                val citiesByParentId = cities.groupBy { it.parentId }
                for (country in visitedNodeCountries) {
                    val cityList: MutableList<BaseNode> = citiesByParentId[country.id]?.mapTo(mutableListOf()) { it.mapModelToBaseNode() }
                        ?: mutableListOf()
                    country.childNode = cityList
                }
                citiesCount = cities.size
                _visitedCountriesWithCities.postValue(visitedNodeCountries)
                _visitedCountries.postValue(visitedCountries.mapModelListToCountryList())
                _visibilityLoader.postValue(GONE)
            }.onFailure { exception ->
                _visibilityLoader.postValue(GONE)
                _errorMessage.postValue(
                    Event(exception.localizedMessage ?: exception.toString()),
                )
            }
        }
    }

    private suspend fun downloadCountries() {
        interactor.downloadCountries().onSuccess {
            showListOfVisitedCountries()
        }.onFailure { exception ->
            _visibilityLoader.postValue(GONE)
            _errorMessage.postValue(
                Event(exception.localizedMessage ?: exception.toString()),
            )
        }
    }

    fun onFloatBtnClicked() {
        _navigateToAllCountries.value = true
    }

    fun onNavigatedToAllCountries() {
        _navigateToAllCountries.value = false
    }

    fun removeFromVisited(country: Country) = viewModelScope.launch {
        _visibilityLoader.postValue(VISIBLE)
        interactor.removeCountryModelFromVisitedList(country.mapToModel()).onSuccess {
            showListOfVisitedCountries()
        }.onFailure { exception ->
            _visibilityLoader.postValue(GONE)
            _errorMessage.postValue(
                Event(exception.localizedMessage ?: exception.toString()),
            )
        }
    }

    fun removeCity(city: City) = viewModelScope.launch {
        _visibilityLoader.postValue(VISIBLE)
        interactor.removeCity(city.mapNodeToModel()).onSuccess {
            showListOfVisitedCountries()
        }.onFailure { exception ->
            _visibilityLoader.postValue(GONE)
            _errorMessage.postValue(
                Event(exception.localizedMessage ?: exception.toString()),
            )
        }
    }
}
