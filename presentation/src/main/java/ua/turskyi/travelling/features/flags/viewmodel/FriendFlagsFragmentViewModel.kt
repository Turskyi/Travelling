package ua.turskyi.travelling.features.flags.viewmodel

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.utils.Event

class FriendFlagsFragmentViewModel : ViewModel() {

    private val _visibilityLoader = MutableLiveData<Int>()
    val visibilityLoader: LiveData<Int> = _visibilityLoader

    private val _visitedCountries = MutableLiveData<List<Country>>()
    val visitedCountries: LiveData<List<Country>> = _visitedCountries

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>> = _errorMessage

    private val db = FirebaseFirestore.getInstance()

    fun setVisitedCountries(userId: String) {
        _visibilityLoader.postValue(VISIBLE)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val countriesRef = db.collection("users").document(userId).collection("visited_countries")
                val querySnapshot = countriesRef.get().await()
                val countriesList = mutableListOf<Country>()

                for (doc in querySnapshot.documents) {
                    val id = when (val idObj = doc.get("id")) {
                        is Number -> idObj.toInt()
                        is String -> idObj.toIntOrNull() ?: doc.id.toIntOrNull() ?: 0
                        else -> doc.id.toIntOrNull() ?: 0
                    }
                    val name = doc.getString("title") ?: doc.getString("name") ?: ""
                    val flag = doc.getString("flag") ?: ""
                    val selfie = doc.getString("selfie") ?: ""
                    val isVisited = doc.getBoolean("isVisited") ?: true

                    countriesList.add(Country(id, name, flag, isVisited, selfie))
                }

                _visitedCountries.postValue(countriesList)
                _visibilityLoader.postValue(GONE)
            } catch (e: Exception) {
                _visibilityLoader.postValue(GONE)
                _errorMessage.postValue(Event(e.localizedMessage ?: e.toString()))
            }
        }
    }
}
