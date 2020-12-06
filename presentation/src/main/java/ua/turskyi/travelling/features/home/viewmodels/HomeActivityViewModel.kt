package ua.turskyi.travelling.features.home.viewmodels

import android.app.Application
import android.content.Intent
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.chad.library.adapter.base.entity.node.BaseNode
import com.firebase.ui.auth.AuthUI
import kotlinx.coroutines.launch
import ua.turskyi.domain.interactor.CountriesInteractor
import ua.turskyi.travelling.R
import ua.turskyi.travelling.common.App
import ua.turskyi.travelling.extensions.*
import ua.turskyi.travelling.models.City
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.models.VisitedCountry
import ua.turskyi.travelling.utils.Event

class HomeActivityViewModel(private val interactor: CountriesInteractor, application: Application) :
    AndroidViewModel(application) {

    var notVisitedCountriesCount: Float = 0F
    var citiesCount = 0

    val isSynchronized: Boolean
        get() = interactor.isSynchronized

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
//        TODO: remove
        interactor.isUpgraded = false
        interactor.isSynchronized = false
    }

    fun upgradeAndSync(authorizationResultLauncher: ActivityResultLauncher<Intent>) {
        _visibilityLoader.postValue(VISIBLE)
        interactor.isUpgraded = true
        if (!interactor.isSynchronized) {
            authorizationResultLauncher.launch(getAuthorizationIntent())
        }
        _visibilityLoader.postValue(GONE)
    }

    private fun getAuthorizationIntent(): Intent {
        /** Choosing authentication providers */
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build()
        )
        return AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            /** Set logo drawable */
            .setLogo(R.drawable.pic_logo)
            .setTheme(R.style.AuthTheme)
            .setTosAndPrivacyPolicyUrls(
//                TODO: replace with Terms of service
                getApplication<App>().getString(R.string.privacy_web_page),
                getApplication<App>().getString(R.string.privacy_web_page)
            )
            .build()
    }

    suspend fun initListOfCountries() {
        val loadCountries: () -> Unit = {
            viewModelScope.launch {
                interactor.getNotVisitedCountriesNum({ notVisitedCountriesNum ->
                    getVisitedCountriesFromDB()
                    notVisitedCountriesCount = notVisitedCountriesNum.toFloat()
                }, { exception ->
                    getVisitedCountriesFromDB()
                    exception.printStackTrace()
                    _errorMessage.run {
                        exception.message?.let { message ->
                            /* Trigger the event by setting a new Event as a new value */
                            postValue(Event(message))
                        }
                    }
                })
            }
        }
        interactor.refreshCountries({ loadCountries() }, { exception ->
            exception.printStackTrace()
            _visibilityLoader.postValue(GONE)
            _errorMessage.run {
                exception.message?.let { message ->
                    /* Trigger the event by setting a new Event as a new value */
                    postValue(Event(message))
                }
            }
            loadCountries()
        })
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
                        }, { exception ->
                            exception.printStackTrace()
                            _visibilityLoader.postValue(GONE)
                            _errorMessage.run {
                                exception.message?.let { message ->
                                    /* Trigger the event by setting a new Event as a new value */
                                    postValue(Event(message))
                                }
                            }
                        })
                    }
                    country.childNode = cityList
                }
                _visitedCountriesWithCities.run { postValue(visitedCountries) }
                _visitedCountries.run { postValue(countries.mapModelListToActualList()) }
                _visibilityLoader.postValue(GONE)
            }, { exception ->
                exception.printStackTrace()
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
        viewModelScope.launch {
            interactor.syncVisitedCountries({
                interactor.isSynchronized = true
                _visibilityLoader.postValue(GONE)
                log("synchronization finished successfully")
            }, { exception ->
                log("synchronization error ${exception.message}")
                exception.printStackTrace()
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
}