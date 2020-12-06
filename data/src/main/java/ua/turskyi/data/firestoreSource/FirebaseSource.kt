package ua.turskyi.data.firestoreSource

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.core.KoinComponent
import ua.turskyi.data.constant.Constants.KEY_IS_VISITED
import ua.turskyi.data.constant.Constants.KEY_SELFIE
import ua.turskyi.data.constant.Constants.LOG
import ua.turskyi.data.constant.Constants.REF_CITIES
import ua.turskyi.data.constant.Constants.REF_COUNTRIES
import ua.turskyi.data.constant.Constants.REF_USERS
import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel


class FirebaseSource : KoinComponent {
    /* init Authentication */
    var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val db = FirebaseFirestore.getInstance()

    private val usersRef: CollectionReference = db.collection(REF_USERS)

    fun insertAllCountries(countries: List<CountryModel>) {
        countries.forEach { countryModel ->
            val country =
                CountryModel(
                    id = countryModel.id,
                    name = countryModel.name,
                    flag = countryModel.flag,
                    isVisited = false,
                    selfie = ""
                )
            usersRef.document("${mFirebaseAuth.currentUser?.email}")
                .collection(REF_COUNTRIES).document(countryModel.id.toString()).set(country)
                .addOnSuccessListener {
                    Log.d("===>", "created country ${country.name}")
                }.addOnFailureListener {
                    it.printStackTrace()
                    Log.d("===>", "exception ${it.message}")
                }
        }
    }

    fun markAsVisited(id: String) {
        val countryRef = usersRef.document("${mFirebaseAuth.currentUser?.email}")
            .collection(REF_COUNTRIES).document(id)
        countryRef.update(KEY_IS_VISITED, true)
            .addOnSuccessListener { Log.d(LOG, "added to visited") }
            .addOnFailureListener { e -> Log.d(LOG, "Error adding to visited", e) }
    }

    fun removeFromVisited(id: String) {
        val countryRef = usersRef.document("${mFirebaseAuth.currentUser?.email}")
            .collection(REF_COUNTRIES).document(id)
        countryRef.update(KEY_IS_VISITED, false)
            .addOnSuccessListener { Log.d(LOG, "country removed from visited") }
            .addOnFailureListener { e -> Log.d(LOG, "Error removing from visited", e) }
    }

    fun updateSelfie(id: String, selfie: String) {
        val countryRef = usersRef.document("${mFirebaseAuth.currentUser?.email}")
            .collection(REF_COUNTRIES).document(id)
        countryRef.update(KEY_SELFIE, selfie)
            .addOnSuccessListener { Log.d(LOG, "selfie successfully updated!") }
            .addOnFailureListener { e -> Log.d(LOG, "Error updating selfie", e) }
    }

    fun insertCity(city: CityModel) {
        usersRef.document("${mFirebaseAuth.currentUser?.email}")
            .collection(REF_CITIES).document(city.id.toString()).set(city)
            .addOnSuccessListener {
                Log.d("===>", "created city ${city.name}")
            }.addOnFailureListener {
                it.printStackTrace()
                Log.d("===>", "exception ${it.message}")
            }
    }

    fun removeCity(id: String) {
        usersRef.document("${mFirebaseAuth.currentUser?.email}")
            .collection(REF_CITIES).document(id)
            .delete()
            .addOnSuccessListener { Log.d(LOG, "city successfully deleted!") }
            .addOnFailureListener { e -> Log.d(LOG, "Error deleting city", e) }
    }

    fun getVisitedCountries(
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        val countriesRef: CollectionReference =
            usersRef.document("${mFirebaseAuth.currentUser?.email}")
                .collection(REF_COUNTRIES)
        countriesRef.whereEqualTo(KEY_IS_VISITED, true).get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                val countries: MutableList<CountryModel> = mutableListOf()
                for (documentSnapshot in queryDocumentSnapshots) {
                    val country: CountryModel =
                        documentSnapshot.toObject(CountryModel::class.java)
                    countries.add(country)
                }
                onSuccess(countries.sortedBy { country -> country.name })
            }
            .addOnFailureListener { e ->
                onError?.invoke(e)
                Log.d(LOG, "Error getting visited countries", e)
            }
    }

    fun getCities(onSuccess: (List<CityModel>) -> Unit, onError: ((Exception) -> Unit?)?) {
        val citiesRef: CollectionReference =
            usersRef.document("${mFirebaseAuth.currentUser?.email}")
                .collection(REF_CITIES)
        citiesRef.get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                val cities: MutableList<CityModel> = mutableListOf()
                for (documentSnapshot in queryDocumentSnapshots) {
                    val city: CityModel =
                        documentSnapshot.toObject(CityModel::class.java)
                    cities.add(city)
                }
                onSuccess(cities.sortedBy { city -> city.name })
            }
            .addOnFailureListener { e ->
                onError?.invoke(e)
                Log.d(LOG, "Error getting cities", e)
            }
    }

    fun getCountNotVisitedCountries(
        onSuccess: (Int) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        val countriesRef: CollectionReference =
            usersRef.document("${mFirebaseAuth.currentUser?.email}")
                .collection(REF_COUNTRIES)
        countriesRef.whereEqualTo(KEY_IS_VISITED, false)
        countriesRef.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(LOG, task.result?.size().toString() + "")
                    task.result?.size()?.let { onSuccess(it) }
                } else {
                    Log.d(LOG, "Error getting count of not visited countries: ", task.exception)
                    task.exception?.let { onError?.invoke(it) }
                }
            }
            .addOnFailureListener { e ->
                onError?.invoke(e)
                Log.d(LOG, "Error getting count of not visited countries: ", e)
            }
    }

    fun getCountriesByRange(
        to: Int, from: Int, onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        val countriesRef: CollectionReference =
            usersRef.document("${mFirebaseAuth.currentUser?.email}")
                .collection(REF_COUNTRIES)
        countriesRef.startAt(from).endBefore(to).get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                val countries: MutableList<CountryModel> = mutableListOf()
                for (documentSnapshot in queryDocumentSnapshots) {
                    val country: CountryModel =
                        documentSnapshot.toObject(CountryModel::class.java)
                    countries.add(country)
                }
                onSuccess(countries.sortedBy { country -> country.name })
            }
            .addOnFailureListener { e ->
                onError?.invoke(e)
                Log.d(LOG, "Error getting paged countries", e)
            }
    }

    fun getCountriesByNameAndRange(
        nameQuery: String?, limit: Int, offset: Int, onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        val countriesRef: CollectionReference =
            usersRef.document("${mFirebaseAuth.currentUser?.email}")
                .collection(REF_COUNTRIES)
        countriesRef.startAt(offset).endBefore(limit).get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                val countries: MutableList<CountryModel> = mutableListOf()
                for (documentSnapshot in queryDocumentSnapshots) {
                    val country: CountryModel =
                        documentSnapshot.toObject(CountryModel::class.java)
                    if(nameQuery != null && country.name.startsWith(nameQuery)){
                        countries.add(country)
                    }
                }
                onSuccess(countries.sortedBy { country -> country.name })
            }
            .addOnFailureListener { e ->
                onError?.invoke(e)
                Log.d(LOG, "Error getting paged countries", e)
            }
    }
}