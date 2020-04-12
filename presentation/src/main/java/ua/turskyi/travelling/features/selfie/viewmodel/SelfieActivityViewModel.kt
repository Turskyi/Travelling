package ua.turskyi.travelling.features.selfie.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ua.turskyi.domain.interactors.CountriesInteractor
import ua.turskyi.travelling.extensions.mapModelListToActualList
import ua.turskyi.travelling.features.selfie.view.fragment.SelfieFragment
import ua.turskyi.travelling.model.Country

class SelfieActivityViewModel(private val interactor: CountriesInteractor) : ViewModel() {
    var visitedCount = 0
    private val _fragmentPosition = MutableLiveData<Int>()
    val fragmentPosition: MutableLiveData<Int>
        get() = _fragmentPosition
    private val _visitedCountries = MutableLiveData<List<Country>>()
    var visitedCountries: LiveData<List<Country>>

    var fragmentsList:MutableList<SelfieFragment> = mutableListOf()

    init {
        visitedCountries = _visitedCountries
    }

    fun getVisitedCountriesFromDB() {
        viewModelScope.launch {
            interactor.getVisitedModelCountries({ countries ->
                 visitedCount = countries.size
                _visitedCountries.run { postValue(countries.mapModelListToActualList()) }
            }, {
//                TODO: What to do if Visited countries was`nt loaded?
            })
        }
    }
}