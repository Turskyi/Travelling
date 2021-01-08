package ua.turskyi.travelling.features.home.viewmodels

import android.app.Application
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.chad.library.adapter.base.entity.node.BaseNode
import kotlinx.coroutines.launch
import ua.turskyi.domain.interactor.CountriesInteractor
import ua.turskyi.domain.model.CountryModel
import ua.turskyi.travelling.extensions.*
import ua.turskyi.travelling.models.City
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.models.VisitedCountry
import ua.turskyi.travelling.utils.Event

class HomeActivityViewModel(private val interactor: CountriesInteractor, application: Application) :
    AndroidViewModel(application) {

    var notVisitedCountriesCount: Float = 0F
    var citiesCount = 0

    //    for future release
    val isSynchronized: Boolean
        get() = interactor.isSynchronized

    //    for future release
    val isUpgraded: Boolean
        get() = interactor.isUpgraded

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

    init {
        _visibilityLoader.postValue(VISIBLE)
    }

//    for future release
//    fun upgradeAndSync(authorizationResultLauncher: ActivityResultLauncher<Intent>) {
//        _visibilityLoader.postValue(VISIBLE)
//        interactor.isUpgraded = true
//        if (!interactor.isSynchronized) {
//            authorizationResultLauncher.launch(getAuthorizationIntent())
//        }
//        _visibilityLoader.postValue(GONE)
//    }
//
//    private fun getAuthorizationIntent(): Intent {
//        /** Choosing authentication providers */
//        val providers = arrayListOf(
//            AuthUI.IdpConfig.GoogleBuilder().build(),
//            AuthUI.IdpConfig.FacebookBuilder().build()
//        )
//        return AuthUI.getInstance()
//            .createSignInIntentBuilder()
//            .setAvailableProviders(providers)
//            /** Set logo drawable */
//            .setLogo(R.drawable.pic_logo)
//            .setTheme(R.style.AuthTheme)
//            .setTosAndPrivacyPolicyUrls(
////                TODO: replace with Terms of service
//                getApplication<App>().getString(R.string.privacy_web_page),
//                getApplication<App>().getString(R.string.privacy_web_page)
//            )
//            .build()
//    }

    val getCountries: () -> Unit = {
        _visibilityLoader.postValue(VISIBLE)
        viewModelScope.launch {
            /** loading count of not visited countries */
            interactor.getNotVisitedCountriesNum({ notVisitedCountriesNum ->
                notVisitedCountriesCount = notVisitedCountriesNum.toFloat()
                /** loading visited countries*/
                getVisitedCountries(notVisitedCountriesNum)
            }, { exception ->
                _errorMessage.run {
                    exception.message?.let { message ->
                        /* Trigger the event by setting a new Event as a new value */
                        postValue(Event(message))
                    }
                }
            })
        }
    }

    private fun getVisitedCountries(notVisitedCountriesNum: Int) {
        viewModelScope.launch {
            interactor.getVisitedModelCountries({ visitedCountries ->
                /** checking if database of visited and not visited countries is empty */
                if (notVisitedCountriesNum == 0 && visitedCountries.isNullOrEmpty()) {
                    viewModelScope.launch {
                        downloadCountries()
                    }
                } else {
                    addCitiesToVisitedCountriesIfNotEmpty(visitedCountries)
                }
            }, { exception ->
                _visibilityLoader.postValue(GONE)
                _errorMessage.run {
                    exception.message?.let { message ->
                        /* Trigger the event by setting a new Event as a new value */
                        postValue(Event(message))
                    }
                }
            })
        }
    }

    private fun addCitiesToVisitedCountriesIfNotEmpty(countries: List<CountryModel>) {
        val visitedCountries = countries.mapModelListToNodeList()
        if (countries.isNullOrEmpty()) {
            _visitedCountriesWithCities.run { postValue(visitedCountries) }
            _visitedCountries.run { postValue(countries.mapModelListToCountryList()) }
            _visibilityLoader.postValue(GONE)
        } else {
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
                        country.childNode = cityList
                        if (country.id == visitedCountries.last().id) {
                            /** showing countries with included cities */
                            _visitedCountriesWithCities.run { postValue(visitedCountries) }
                            _visitedCountries.run { postValue(countries.mapModelListToCountryList()) }
                            _visibilityLoader.postValue(GONE)
                        }
                    }, { exception ->
                        _visibilityLoader.postValue(GONE)
                        _errorMessage.run {
                            exception.message?.let { message ->
                                /* Trigger the event by setting a new Event as a new value */
                                postValue(Event(message))
                            }
                        }
                    })
                }
            }
            /* do not write any logic after  countries loop (here),
             * rest of the logic must be in "get cities" success method ,
             * since it started later then here */
        }
    }

    fun showListOfCountries() = getCountries()

    private suspend fun downloadCountries() =
        interactor.downloadCountries({  showListOfCountries() }, { exception ->
            _visibilityLoader.postValue(GONE)
            _errorMessage.run {
                exception.message?.let { message ->
                    /* Trigger the event by setting a new Event as a new value */
                    postValue(Event(message))
                }
            }
        })

    fun onFloatBtnClicked() {
        _navigateToAllCountries.value = true
    }

    fun onNavigatedToAllCountries() {
        _navigateToAllCountries.value = false
    }

    fun removeFromVisited(country: Country) = viewModelScope.launch {
        _visibilityLoader.postValue(VISIBLE)
            interactor.removeCountryModelFromVisitedList(country.mapToModel(),{
                showListOfCountries()
            },{ exception ->
                _visibilityLoader.postValue(GONE)
                _errorMessage.run {
                    exception.message?.let { message ->
                        /* Trigger the event by setting a new Event as a new value */
                        postValue(Event(message))
                    }
                }
            })
        }

    fun removeCity(city: City) = viewModelScope.launch {
        _visibilityLoader.postValue(VISIBLE)
            interactor.removeCity(city.mapNodeToModel(), {
                showListOfCountries()
            }, { exception ->
                _visibilityLoader.postValue(GONE)
                _errorMessage.run {
                    exception.message?.let { message ->
                        /* Trigger the event by setting a new Event as a new value */
                        postValue(Event(message))
                    }
                }
            })
        }

    //    for future release
//    fun syncDatabaseWithFireStore() {
//        _visibilityLoader.postValue(VISIBLE)
//        viewModelScope.launch {
//            interactor.syncVisitedCountries({
//                interactor.isSynchronized = true
//                _visibilityLoader.postValue(GONE)
//            }, { exception ->
//                _visibilityLoader.postValue(GONE)
//                _errorMessage.run {
//                    exception.message?.let { message ->
//                        /* Trigger the event by setting a new Event as a new value */
//                        postValue(Event(message))
//                    }
//                }
//            })
//        }
//    }
}