package ua.turskyi.travelling.features.flags.viewmodel

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ua.turskyi.domain.interactor.CountriesInteractor
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.utils.Event
import ua.turskyi.travelling.utils.extensions.mapModelListToCountryList

class FlagsFragmentViewModel(private val interactor: CountriesInteractor) : ViewModel(),
    LifecycleEventObserver {
    var userId: String? = null
    private var visitedCount = 0

    private val _visibilityLoader = MutableLiveData<Int>()
    val visibilityLoader: MutableLiveData<Int>
        get() = _visibilityLoader

    val visitedCountries: LiveData<List<Country>>
        field = MutableLiveData<List<Country>>()

    val errorMessage: LiveData<Event<String>>
        field = MutableLiveData<Event<String>>()

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_CREATE) {
            setVisitedCountries(userId)
        }
    }

    fun setVisitedCountries(targetUserId: String? = userId) {
        if (targetUserId.isNullOrEmpty()) {
            viewModelScope.launch {
                interactor.setVisitedCountries().onSuccess { countries ->
                    visitedCount = countries.size
                    visitedCountries.postValue(countries.mapModelListToCountryList())
                    _visibilityLoader.postValue(View.GONE)
                }.onFailure { exception ->
                    _visibilityLoader.postValue(View.GONE)
                    errorMessage.postValue(
                        Event(
                            exception.localizedMessage ?: exception.toString()
                        )
                    )
                }
            }
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                _visibilityLoader.postValue(View.VISIBLE)
                try {
                    val db = FirebaseFirestore.getInstance()
                    val countriesRef = db.collection("users").document(targetUserId).collection("visited_countries")
                    val querySnapshot = countriesRef.get().await()
                    val countriesList = mutableListOf<Country>()
                    for (doc in querySnapshot.documents) {
                        val id = doc.getLong("id")?.toInt() ?: doc.id.toIntOrNull() ?: 0
                        val title = doc.getString("title") ?: doc.getString("name") ?: ""
                        val flag = doc.getString("flag") ?: ""
                        val selfie = doc.getString("selfie") ?: ""
                        val isVisited = doc.getBoolean("isVisited") ?: true
                        countriesList.add(Country(id, title, flag, isVisited, selfie))
                    }
                    visitedCount = countriesList.size
                    visitedCountries.postValue(countriesList)
                    _visibilityLoader.postValue(View.GONE)
                } catch (e: Exception) {
                    _visibilityLoader.postValue(View.GONE)
                    errorMessage.postValue(
                        Event(
                            e.localizedMessage ?: e.toString()
                        )
                    )
                }
            }
        }
    }

    fun updateSelfie(id: Int, filePath: String) {
        if (filePath.isNotEmpty()) {
            _visibilityLoader.postValue(View.VISIBLE)
        }
        viewModelScope.launch {
            interactor.updateSelfie(
                id = id,
                filePath = filePath,
            ).onSuccess { countries ->
                visitedCountries.postValue(countries.mapModelListToCountryList())
                _visibilityLoader.postValue(View.GONE)
            }.onFailure { exception ->
                _visibilityLoader.postValue(View.GONE)
                errorMessage.postValue(
                    Event(
                        exception.localizedMessage ?: exception.toString()
                    )
                )
            }
        }
    }
}
