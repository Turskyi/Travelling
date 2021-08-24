package ua.turskyi.travelling.features.flags.viewmodel

import android.view.View
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import ua.turskyi.domain.interactor.CountriesInteractor
import ua.turskyi.travelling.utils.extensions.mapModelListToCountryList
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.utils.Event

class FlagsFragmentViewModel(private val interactor: CountriesInteractor) : ViewModel(),
    LifecycleObserver {
    private var visitedCount = 0

    private val _visibilityLoader = MutableLiveData<Int>()
    val visibilityLoader: MutableLiveData<Int>
        get() = _visibilityLoader

    private val _visitedCountries = MutableLiveData<List<Country>>()
    val visitedCountries: LiveData<List<Country>>
        get() = _visitedCountries

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>>
        get() = _errorMessage

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun getVisitedCountriesFromDB() {
        viewModelScope.launch {
            interactor.setVisitedCountries({ countries ->
                visitedCount = countries.size
                _visitedCountries.run { postValue(countries.mapModelListToCountryList()) }
                _visibilityLoader.postValue(View.GONE)
            }, { exception ->
                _visibilityLoader.postValue(View.GONE)
                _errorMessage.run {
                    exception.message?.let { message ->
                        // Triggering the event by setting a new Event as a new value
                        postValue(Event(message))
                    }
                }
            })
        }
    }

    fun updateSelfie(id: Int, selfie: String) {
        _visibilityLoader.postValue(View.VISIBLE)
        viewModelScope.launch(IO) {
            interactor.updateSelfie(id, selfie, { countries ->
                _visitedCountries.run { postValue(countries.mapModelListToCountryList()) }
                _visibilityLoader.postValue(View.GONE)
            }, { exception ->
                _visibilityLoader.postValue(View.GONE)
                _errorMessage.run {
                    exception.message?.let { message ->
                        // Triggering the event by setting a new Event as a new value
                        postValue(Event(message))
                    }
                }
            })
        }
    }
}