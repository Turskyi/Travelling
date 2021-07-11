package ua.turskyi.data.firestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import org.koin.core.component.KoinComponent
import ua.turskyi.data.entities.room.CityEntity
import ua.turskyi.data.entities.room.CountryEntity
import ua.turskyi.data.extensions.mapCountryToVisitedCountry
import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel

class FirestoreSource : KoinComponent {
    companion object {
        // constants for firestore
        const val REF_USERS = "users"
        const val REF_COUNTRIES = "countries"
        const val REF_VISITED_COUNTRIES = "visited_countries"
        const val REF_CITIES = "cities"
        const val KEY_IS_VISITED = "isVisited"
        const val KEY_SELFIE = "selfie"
        const val KEY_PARENT_ID = "parentId"
    }
    //init Authentication
    var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val db = FirebaseFirestore.getInstance()

    private val usersRef: CollectionReference = db.collection(REF_USERS)

    fun insertAllCountries(
        countries: List<CountryEntity>,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = countries.forEachIndexed { index, countryEntity ->
        val country = CountryEntity(
            id = index,
            name = countryEntity.name,
            flag = countryEntity.flag,
            isVisited = false,
        )
        usersRef.document("${mFirebaseAuth.currentUser?.uid}")
            .collection(REF_COUNTRIES).document(countryEntity.name).set(country)
            .addOnSuccessListener {
                if (index == countries.size - 1) {
                    onSuccess()
                }
            }.addOnFailureListener { exception ->
                onError?.invoke(exception)
            }
    }

    fun markAsVisited(
        countryEntity: CountryEntity,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        // set mark "isVisited = true" in list of all countries
        val countryRef: DocumentReference = usersRef.document("${mFirebaseAuth.currentUser?.uid}")
            .collection(REF_COUNTRIES).document(countryEntity.name)
        countryRef.update(KEY_IS_VISITED, true)
            .addOnSuccessListener {
                /** making copy of the country and adding to a new list of visited countries */
                usersRef.document("${mFirebaseAuth.currentUser?.uid}")
                    .collection(REF_VISITED_COUNTRIES).document(countryEntity.name)
                    .set(countryEntity.mapCountryToVisitedCountry())
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { exception -> onError?.invoke(exception) }
            }
            .addOnFailureListener { exception -> onError?.invoke(exception) }
    }

    fun removeFromVisited(
        name: String,
        parentId: Int,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        /** deleting from list of visited countries */
        usersRef.document("${mFirebaseAuth.currentUser?.uid}")
            .collection(REF_VISITED_COUNTRIES).document(name)
            .delete()
            .addOnSuccessListener {
                /** set mark "isVisited = false" in list of all countries */
                val countryRef = usersRef.document("${mFirebaseAuth.currentUser?.uid}")
                    .collection(REF_COUNTRIES).document(name)
                countryRef.update(KEY_IS_VISITED, false)
                    .addOnSuccessListener {
                        val visitedCities: CollectionReference =
                            usersRef.document("${mFirebaseAuth.currentUser?.uid}")
                                .collection(REF_CITIES)
                        /** getting visited cities of deleted visited country */
                        visitedCities.whereEqualTo(KEY_PARENT_ID, parentId)
                            .get().addOnSuccessListener { queryDocumentSnapshots ->
                                if (queryDocumentSnapshots.size() == 0) {
                                    onSuccess()
                                } else {
                                    /* Getting a new write batch and commit all write operations */
                                    val batch: WriteBatch = db.batch()
                                    /** delete every visited city of deleted visited country */
                                    for (documentSnapshot in queryDocumentSnapshots) {
                                        batch.delete(documentSnapshot.reference)
                                    }
                                    batch.commit().addOnSuccessListener { onSuccess() }
                                        .addOnFailureListener { exception ->
                                            onError?.invoke(exception)
                                        }
                                }
                            }.addOnFailureListener { exception -> onError?.invoke(exception) }
                    }.addOnFailureListener { exception -> onError?.invoke(exception) }
            }.addOnFailureListener { exception -> onError?.invoke(exception) }
    }

    fun updateSelfie(id: String, selfie: String) {
        val countryRef = usersRef.document("${mFirebaseAuth.currentUser?.email}")
            .collection(REF_COUNTRIES).document(id)
        countryRef.update(KEY_SELFIE, selfie)
            .addOnSuccessListener {  }
            .addOnFailureListener { }
    }

    fun insertCity(city: CityEntity,
                   onSuccess: () -> Unit,
                   onError: ((Exception) -> Unit?)?) {
        usersRef.document("${mFirebaseAuth.currentUser?.uid}")
            .collection(REF_CITIES).document(city.name).set(city)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onError?.invoke(exception) }
    }

    fun removeCity(
        name: String,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        usersRef.document("${mFirebaseAuth.currentUser?.uid}")
            .collection(REF_CITIES).document(name)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onError?.invoke(exception) }
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
                    task.result?.size()?.let { onSuccess(it) }
                } else {
                    task.exception?.let { onError?.invoke(it) }
                }
            }
            .addOnFailureListener { e ->
                onError?.invoke(e)
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
                    if (nameQuery != null && country.name.startsWith(nameQuery)) {
                        countries.add(country)
                    }
                }
                onSuccess(countries.sortedBy { country -> country.name })
            }
            .addOnFailureListener { e ->
                onError?.invoke(e)
            }
    }
}