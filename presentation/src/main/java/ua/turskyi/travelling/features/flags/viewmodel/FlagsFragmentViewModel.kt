package ua.turskyi.travelling.features.flags.viewmodel

import android.view.View
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import ua.turskyi.domain.interactor.CountriesInteractor
import ua.turskyi.domain.model.CountryModel
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.utils.Event
import ua.turskyi.travelling.utils.extensions.mapModelListToCountryList

class FlagsFragmentViewModel(private val interactor: CountriesInteractor) : ViewModel(),
    LifecycleEventObserver {
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

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_CREATE) {
            setVisitedCountries()
        }
    }

    private fun setVisitedCountries() {
        viewModelScope.launch {
            interactor.setVisitedCountries(
                { countries: List<CountryModel> ->
                    visitedCount = countries.size
                    _visitedCountries.postValue(countries.mapModelListToCountryList())
                    _visibilityLoader.postValue(View.GONE)
                },
                onError = { exception: Exception /* = java.lang.Exception */ ->
                    _visibilityLoader.postValue(View.GONE)
                    _errorMessage.run {
                        // Trigger the event by setting a new Event as a new value
                        postValue(
                            Event(
                                exception.localizedMessage ?: exception.stackTraceToString()
                            )
                        )
                    }
                },
            )
        }
    }

    fun updateSelfie(id: Int, filePath: String) {
        _visibilityLoader.postValue(View.VISIBLE)
        viewModelScope.launch(IO) {
            interactor.updateSelfie(
                id = id,
                filePath = filePath,
                onSuccess = { countries: List<CountryModel> ->
                    _visitedCountries.postValue(countries.mapModelListToCountryList())
                    _visibilityLoader.postValue(View.GONE)
                },
                onError = { exception: Exception ->
                    _visibilityLoader.postValue(View.GONE)
                    _errorMessage.run {
                        // Trigger the event by setting a new Event as a new value
                        postValue(
                            Event(
                                exception.localizedMessage ?: exception.stackTraceToString()
                            )
                        )
                    }
                },
            )
        }
    }
}