package ua.turskyi.travelling.features.flags.viewmodel

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import ua.turskyi.domain.interactor.CountriesInteractor
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
            interactor.setVisitedCountries().onSuccess { countries ->
                visitedCount = countries.size
                _visitedCountries.postValue(countries.mapModelListToCountryList())
                _visibilityLoader.postValue(View.GONE)
            }.onFailure { exception ->
                _visibilityLoader.postValue(View.GONE)
                _errorMessage.postValue(
                    Event(
                        exception.localizedMessage ?: exception.toString()
                    )
                )
            }
        }
    }

    fun updateSelfie(id: Int, filePath: String) {
        _visibilityLoader.postValue(View.VISIBLE)
        viewModelScope.launch(IO) {
            interactor.updateSelfie(
                id = id,
                filePath = filePath,
            ).onSuccess { countries ->
                _visitedCountries.postValue(countries.mapModelListToCountryList())
                _visibilityLoader.postValue(View.GONE)
            }.onFailure { exception ->
                _visibilityLoader.postValue(View.GONE)
                _errorMessage.postValue(
                    Event(
                        exception.localizedMessage ?: exception.toString()
                    )
                )
            }
        }
    }
}
