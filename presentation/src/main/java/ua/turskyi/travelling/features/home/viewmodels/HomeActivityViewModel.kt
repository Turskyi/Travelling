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
import ua.turskyi.travelling.utils.extensions.*

class HomeActivityViewModel(private val interactor: CountriesInteractor) : ViewModel() {

    var notVisitedCountriesCount: Float = 0F
    var citiesCount = 0
    var isPermissionGranted: Boolean = false
    var isDoubleBackToExitPressed = false
    var mLastClickTime: Long = 0

    private val _visibilityLoader = MutableLiveData<Int>()
    val visibilityLoader: MutableLiveData<Int>
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
            // loading count of not visited countries
            interactor.setNotVisitedCountriesNum({ notVisitedCountriesNum ->
                notVisitedCountriesCount = notVisitedCountriesNum.toFloat()
                // loading visited countries
                setVisitedCountries(notVisitedCountriesNum)
            }, { exception: Exception /* = java.lang.Exception */ ->
                // Trigger the event by setting a new Event as a new value
                _errorMessage.postValue(
                    Event(exception.localizedMessage ?: exception.stackTraceToString()),
                )
            })
        }
    }

    private fun setVisitedCountries(notVisitedCountriesNum: Int) {
        viewModelScope.launch {
            interactor.setVisitedCountries({ visitedCountries: List<CountryModel> ->
                // checking if database of visited and not visited countries is empty
                if (notVisitedCountriesNum == 0 && visitedCountries.isEmpty()) {
                    viewModelScope.launch { downloadCountries() }
                } else {
                    val visitedNodeCountries: MutableList<VisitedCountry> =
                        visitedCountries.mapModelListToNodeList()
                    if (visitedNodeCountries.isEmpty()) {
                        _visitedCountriesWithCities.postValue(visitedNodeCountries)

                        _visitedCountries.postValue(visitedCountries.mapModelListToCountryList())

                        _visibilityLoader.postValue(GONE)
                    } else {
                        addCitiesToVisitedCountries(visitedNodeCountries, visitedCountries)
                        /* do not write any logic after  countries loop (here),
                         * rest of the logic must be in "get cities" success method ,
                         * since it started later then here */
                    }
                }
            }, { exception: Exception /* = java.lang.Exception */ ->
                _visibilityLoader.postValue(GONE)
                // Trigger the event by setting a new Event as a new value
                _errorMessage.postValue(
                    Event(exception.localizedMessage ?: exception.stackTraceToString()),
                )
            })
        }
    }

    private fun addCitiesToVisitedCountries(
        visitedNodeCountries: MutableList<VisitedCountry>,
        visitedCountries: List<CountryModel>
    ) {
        for (country: VisitedCountry in visitedNodeCountries) {
            val cityList: MutableList<BaseNode> = mutableListOf()
            viewModelScope.launch {
                interactor.setCities({ cities ->
                    for (city in cities) {
                        if (country.id == city.parentId) {
                            cityList.add(city.mapModelToBaseNode())
                        }
                    }
                    citiesCount = cities.size
                    country.childNode = cityList
                    if (country.id == visitedCountries.last().id) {
                        // showing countries with included cities
                        _visitedCountriesWithCities.run { postValue(visitedNodeCountries) }
                        _visitedCountries.postValue(visitedCountries.mapModelListToCountryList())
                        _visibilityLoader.postValue(GONE)
                    }
                }, { exception: Exception /* = java.lang.Exception */ ->
                    _visibilityLoader.postValue(GONE)
                    // Trigger the event by setting a new Event as a new value
                    _errorMessage.postValue(
                        Event(
                            exception.localizedMessage ?: exception.stackTraceToString(),
                        ),
                    )
                })
            }
        }
    }

    private suspend fun downloadCountries() {
        interactor.downloadCountries(
            onSuccess = { showListOfVisitedCountries() },
            onError = { exception: Exception /* = java.lang.Exception */ ->
                _visibilityLoader.postValue(GONE)
                // Trigger the event by setting a new Event as a new value
                _errorMessage.postValue(
                    Event(exception.localizedMessage ?: exception.stackTraceToString()),
                )
            },
        )
    }

    fun onFloatBtnClicked() {
        _navigateToAllCountries.value = true
    }

    fun onNavigatedToAllCountries() {
        _navigateToAllCountries.value = false
    }

    fun removeFromVisited(country: Country) = viewModelScope.launch {
        _visibilityLoader.postValue(VISIBLE)
        interactor.removeCountryModelFromVisitedList(country.mapToModel(), {
            showListOfVisitedCountries()
        }, { exception: Exception /* = java.lang.Exception */ ->
            _visibilityLoader.postValue(GONE)
            // Trigger the event by setting a new Event as a new value
            _errorMessage.postValue(
                Event(exception.localizedMessage ?: exception.stackTraceToString()),
            )
        })
    }

    fun removeCity(city: City) = viewModelScope.launch {
        _visibilityLoader.postValue(VISIBLE)
        interactor.removeCity(city.mapNodeToModel(), {
            showListOfVisitedCountries()
        }, { exception: Exception /* = java.lang.Exception */ ->
            _visibilityLoader.postValue(GONE)
            // Trigger the event by setting a new Event as a new value
            _errorMessage.postValue(
                Event(exception.localizedMessage ?: exception.stackTraceToString()),
            )
        })
    }
}