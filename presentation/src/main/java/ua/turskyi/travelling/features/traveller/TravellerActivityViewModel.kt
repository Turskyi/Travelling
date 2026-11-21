package ua.turskyi.travelling.features.traveller

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ua.turskyi.travelling.entities.VisitedCountryNode
import ua.turskyi.travelling.models.City
import ua.turskyi.travelling.models.VisitedCountry
import ua.turskyi.travelling.utils.Event

class TravellerActivityViewModel : ViewModel() {

    var cityCount: Int = 0
    var mLastClickTime: Long = 0

    private val _visibilityLoader = MutableLiveData<Int>()
    val visibilityLoader: LiveData<Int> = _visibilityLoader

    private val _visitedCountries = MutableLiveData<List<VisitedCountry>>()
    val visitedCountries: LiveData<List<VisitedCountry>> = _visitedCountries

    private val _visitedCountriesWithCitiesNode = MutableLiveData<List<VisitedCountryNode>>()
    val visitedCountriesWithCitiesNode: LiveData<List<VisitedCountryNode>> =
        _visitedCountriesWithCitiesNode

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>> = _errorMessage

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun showListOfVisitedCountriesById(userId: String) {
        _visibilityLoader.postValue(VISIBLE)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val countriesRef =
                    db.collection("users").document(userId).collection("visited_countries")
                val querySnapshot = countriesRef.get().await()
                val visitedCountriesList = mutableListOf<VisitedCountry>()
                val nodesList = mutableListOf<VisitedCountryNode>()

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

                    val country = VisitedCountry(id, title, flag, isVisited, selfie)
                    visitedCountriesList.add(country)

                    val node = VisitedCountryNode(id, "", title, flag, selfie, "")

                    try {
                        val citiesSnapshot =
                            countriesRef.document(doc.id).collection("cities").get().await()
                        val cityNodes =
                            mutableListOf<com.chad.library.adapter.base.entity.node.BaseNode>()
                        for (cityDoc in citiesSnapshot.documents) {
                            val cityId = when (val cityIdObj = cityDoc.get("id")) {
                                is Number -> cityIdObj.toInt()
                                is String -> cityIdObj.toIntOrNull() ?: 0
                                else -> 0
                            }
                            val cityName = cityDoc.getString("name") ?: ""
                            val parentId = when (val parentIdObj = cityDoc.get("parentId")) {
                                is Number -> parentIdObj.toInt()
                                is String -> parentIdObj.toIntOrNull() ?: id
                                else -> id
                            }
                            val month = cityDoc.getString("month") ?: ""
                            cityNodes.add(City(cityId, cityName, parentId, month))
                        }
                        node.childNode = cityNodes
                    } catch (_: Exception) {
                    }

                    nodesList.add(node)
                }

                _visitedCountries.postValue(visitedCountriesList.distinctBy { it.title.lowercase().trim() })
                _visitedCountriesWithCitiesNode.postValue(nodesList.distinctBy { it.title.lowercase().trim() })
                _visibilityLoader.postValue(GONE)
            } catch (e: Exception) {
                _visibilityLoader.postValue(GONE)
                _errorMessage.postValue(Event(e.localizedMessage ?: e.toString()))
            }
        }
    }
}
