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
import com.google.firebase.storage.FirebaseStorage
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

    val visibilityLoader: LiveData<Int>
        field = MutableLiveData<Int>()

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
                    visibilityLoader.postValue(View.GONE)
                }.onFailure { exception ->
                    visibilityLoader.postValue(View.GONE)
                    errorMessage.postValue(
                        Event(
                            exception.localizedMessage ?: exception.toString()
                        )
                    )
                }
            }
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                visibilityLoader.postValue(View.VISIBLE)
                try {
                    val db = FirebaseFirestore.getInstance()
                    val storage = FirebaseStorage.getInstance()
                    val countriesRef = db.collection("users").document(targetUserId).collection("visited_countries")
                    val querySnapshot = countriesRef.get().await()
                    val countriesList = mutableListOf<Country>()
                    for (doc in querySnapshot.documents) {
                        val id = when (val idObj = doc.get("id")) {
                            is Number -> idObj.toInt()
                            is String -> idObj.toIntOrNull() ?: doc.id.toIntOrNull() ?: 0
                            else -> doc.id.toIntOrNull() ?: 0
                        }
                        val title = doc.getString("title") ?: doc.getString("name") ?: ""
                        val flag = doc.getString("flag") ?: ""
                        var selfie = doc.getString("selfie") ?: ""
                        if (selfie.isNotEmpty() && selfie.startsWith("cloud_sync/")) {
                            try {
                                selfie = storage.reference.child(selfie).downloadUrl.await().toString()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        val isVisited = doc.getBoolean("isVisited") ?: true
                        countriesList.add(Country(id, title, flag, isVisited, selfie))
                    }
                    val distinctCountries = countriesList.distinctBy { it.name.lowercase().trim() }
                    visitedCount = distinctCountries.size
                    visitedCountries.postValue(distinctCountries)
                    visibilityLoader.postValue(View.GONE)
                } catch (e: Exception) {
                    visibilityLoader.postValue(View.GONE)
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
            visibilityLoader.postValue(View.VISIBLE)
        }
        viewModelScope.launch {
            interactor.updateSelfie(
                id = id,
                filePath = filePath,
            ).onSuccess { countries ->
                visitedCountries.postValue(countries.mapModelListToCountryList())
                visibilityLoader.postValue(View.GONE)
            }.onFailure { exception ->
                visibilityLoader.postValue(View.GONE)
                errorMessage.postValue(
                    Event(
                        exception.localizedMessage ?: exception.toString()
                    )
                )
            }
        }
    }
}
