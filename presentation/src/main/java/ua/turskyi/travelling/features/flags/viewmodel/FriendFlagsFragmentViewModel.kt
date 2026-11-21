package ua.turskyi.travelling.features.flags.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ua.turskyi.travelling.models.VisitedCountry
import ua.turskyi.travelling.utils.Event

class FriendFlagsFragmentViewModel : ViewModel() {

    private val _visibilityLoader: MutableLiveData<Int> = MutableLiveData<Int>()
    val visibilityLoader: MutableLiveData<Int>
        get() = _visibilityLoader

    val visitedCountries: LiveData<List<VisitedCountry>>
        field: MutableLiveData<List<VisitedCountry>> = MutableLiveData<List<VisitedCountry>>()

    val errorMessage: LiveData<Event<String>>
        field: MutableLiveData<Event<String>> = MutableLiveData<Event<String>>()

}
