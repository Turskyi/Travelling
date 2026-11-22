package ua.turskyi.data.datasources.local.firestore

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import org.koin.core.component.KoinComponent
import ua.turskyi.data.datasources.local.entities.TravellerEntity
import ua.turskyi.data.util.exceptions.NotFoundException

class FirestoreDatabaseSourceImpl : KoinComponent,
    FirestoreDatabaseSource {
    companion object {
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_VISITED_COUNTRIES = "visited_countries"
        private const val KEY_IS_VISIBLE = "isVisible"
        private const val KEY_NAME = "name"
        private const val KEY_AVATAR = "avatar"
        private const val KEY_COUNTER = "counter"
        private const val KEY_IS_ADMIN = "isAdmin"
    }

    private val mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val currentUser: FirebaseUser? get() = mFirebaseAuth.currentUser
    private val usersRef: CollectionReference = database.collection(COLLECTION_USERS)

    override suspend fun saveTraveller(onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val user = currentUser
        if (user != null) {
            val userRef: DocumentReference = usersRef.document(user.uid)
            val traveller = if (user.displayName != null && user.photoUrl != null) {
                TravellerEntity(
                    id = userRef.id,
                    name = user.displayName!!,
                    avatar = user.photoUrl.toString(),
                    counter = 0,
                    isVisible = false,
                )
            } else if (user.displayName != null && user.photoUrl == null) {
                TravellerEntity(
                    id = userRef.id,
                    name = user.displayName!!,
                    avatar = "",
                    counter = 0,
                    isVisible = false,
                )
            } else if (user.photoUrl != null && user.displayName == null) {
                TravellerEntity(id = userRef.id, avatar = user.photoUrl.toString())
            } else {
                TravellerEntity(id = userRef.id)
            }
            userRef.set(traveller)
                .addOnSuccessListener { onSuccess.invoke() }
                .addOnFailureListener { e -> onError.invoke(e) }
        } else {
            mFirebaseAuth.signOut()
            onError.invoke(NotFoundException())
        }
    }

    override suspend fun setUserVisibility(
        visible: Boolean,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val user = currentUser
        if (user != null) {
            val userRef: DocumentReference = usersRef.document(user.uid)
            userRef.update(KEY_IS_VISIBLE, visible)
                .addOnSuccessListener { onSuccess.invoke() }
                .addOnFailureListener { exception: Exception ->
                    onError.invoke(exception)
                }
        } else {
            mFirebaseAuth.signOut()
            onError.invoke(NotFoundException())
        }
    }

    override suspend fun setUserVisibility(
        onSuccess: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val user = currentUser
        if (user != null) {
            val userRef: DocumentReference = usersRef.document(user.uid)
            userRef.get()
                .addOnSuccessListener { document: DocumentSnapshot ->
                    if (document.getBoolean(KEY_IS_VISIBLE) != null) {
                        val isVisible: Boolean? = document.getBoolean(KEY_IS_VISIBLE)
                        onSuccess(isVisible ?: false)
                    } else {
                        onError.invoke(NotFoundException())
                    }
                }
                .addOnFailureListener { exception: Exception ->
                    onError.invoke(exception)
                }
        } else {
            mFirebaseAuth.signOut()
            onError.invoke(NotFoundException())
        }
    }

    override suspend fun setTravellersByRange(
        to: Long,
        from: Int,
        onSuccess: (List<TravellerEntity>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val user = currentUser
        if (user != null) {
            val userRef: DocumentReference = usersRef.document(user.uid)
            userRef.get()
                .addOnSuccessListener(Dispatchers.IO.asExecutor()) { document: DocumentSnapshot ->
                    val isAdmin = document.getBoolean(KEY_IS_ADMIN) ?: false

                    val visibleQuery: Query = usersRef.whereEqualTo(KEY_IS_VISIBLE, true)

                    val taskWithAllTravellers: Task<QuerySnapshot> = usersRef.limit(to).get()
                    val taskWithVisibleTravellers: Task<QuerySnapshot> = visibleQuery.limit(to).get()

                    val taskWithTravellers = if (isAdmin) {
                        taskWithAllTravellers
                    } else {
                        taskWithVisibleTravellers
                    }
                    taskWithTravellers
                        .addOnSuccessListener(Dispatchers.IO.asExecutor()) { queryDocumentSnapshots: QuerySnapshot ->
                            val travellers: MutableList<TravellerEntity> = mutableListOf()
                            for (i in from until queryDocumentSnapshots.size()) {
                                val snapshot: DocumentSnapshot = queryDocumentSnapshots.documents[i]
                                travellers.add(
                                    TravellerEntity(
                                        id = snapshot.id,
                                        name = snapshot[KEY_NAME] as? String ?: "",
                                        avatar = snapshot[KEY_AVATAR] as? String ?: "",
                                        counter = snapshot[KEY_COUNTER] as? Long ?: 0L,
                                        isVisible = snapshot[KEY_IS_VISIBLE] as? Boolean ?: false,
                                    )
                                )
                            }

                            travellers.sortByDescending { traveller: TravellerEntity ->
                                traveller.counter
                            }
                            onSuccess(travellers)
                        }.addOnFailureListener(Dispatchers.IO.asExecutor()) { exception: Exception ->
                            onError.invoke(exception)
                        }
                }
                .addOnFailureListener(Dispatchers.IO.asExecutor()) { exception: Exception ->
                    onError.invoke(exception)
                }
        } else {
            mFirebaseAuth.signOut()
            onError.invoke(NotFoundException())
        }
    }

    override suspend fun setTravellersByName(
        nameQuery: String,
        requestedLoadSize: Long,
        requestedStartPosition: Int,
        onSuccess: (List<TravellerEntity>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val usersRef: Query = usersRef
        usersRef.whereEqualTo(KEY_IS_VISIBLE, true)
            .limit(requestedLoadSize)
            .get()
            .addOnSuccessListener(Dispatchers.IO.asExecutor()) { queryDocumentSnapshots ->
                val travellers: MutableList<TravellerEntity> = mutableListOf()
                for (i in requestedStartPosition until queryDocumentSnapshots.size()) {
                    val snapshot: DocumentSnapshot = queryDocumentSnapshots.documents[i]
                    val traveller = TravellerEntity(
                        id = snapshot.id,
                        name = snapshot[KEY_NAME] as? String ?: "",
                        avatar = snapshot[KEY_AVATAR] as? String ?: "",
                        counter = snapshot[KEY_COUNTER] as? Long ?: 0L,
                        isVisible = snapshot[KEY_IS_VISIBLE] as? Boolean ?: false,
                    )
                    if (traveller.name.startsWith(nameQuery, ignoreCase = true)) {
                        travellers.add(traveller)
                    }
                }
                travellers.sortBy { item: TravellerEntity -> item.name }
                onSuccess(travellers)
            }.addOnFailureListener(Dispatchers.IO.asExecutor()) { exception -> onError.invoke(exception) }
    }

    override suspend fun setTopTravellersPercent(
        onSuccess: (Int) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val user = currentUser
        if (user != null) {
            val visitedCountriesRef: CollectionReference = usersRef.document(
                user.uid,
            ).collection(COLLECTION_VISITED_COUNTRIES)

            visitedCountriesRef.get()
                .addOnSuccessListener(Dispatchers.IO.asExecutor()) { queryDocumentSnapshots: QuerySnapshot ->
                    val countOfVisitedCountries: Int = queryDocumentSnapshots.size()
                    if (countOfVisitedCountries == 0) {
                        onSuccess(100)
                    } else {
                        usersRef.get().addOnSuccessListener(Dispatchers.IO.asExecutor()) { snapshots: QuerySnapshot ->
                            val userCount: Int = snapshots.size()
                            usersRef.whereGreaterThan(KEY_COUNTER, countOfVisitedCountries).get()
                                .addOnSuccessListener(Dispatchers.IO.asExecutor()) { documents: QuerySnapshot ->
                                    if (userCount == 0) {
                                        onSuccess(100)
                                    } else {
                                        val countOfTopTravellers: Int = documents.size()
                                        onSuccess(countOfTopTravellers * 100 / userCount)
                                    }
                                }.addOnFailureListener(Dispatchers.IO.asExecutor()) { exception: Exception ->
                                    onError.invoke(exception)
                                }
                        }.addOnFailureListener(Dispatchers.IO.asExecutor()) { exception: Exception ->
                            onError.invoke(exception)
                        }
                    }
                }.addOnFailureListener(Dispatchers.IO.asExecutor()) { exception: Exception ->
                    onError.invoke(exception)
                }
        } else {
            mFirebaseAuth.signOut()
            onError.invoke(NotFoundException())
        }
    }
}
