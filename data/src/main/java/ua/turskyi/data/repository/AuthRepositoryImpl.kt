package ua.turskyi.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import ua.turskyi.domain.model.UserAccountModel
import ua.turskyi.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val context: Context,
) : AuthRepository, KoinComponent {

    companion object {
        private const val PREFS_NAME = "ua.turskyi.travelling.auth_prefs"
        private const val KEY_KEEP_SIGNED_IN = "key_keep_signed_in"
    }

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun getCurrentUser(): UserAccountModel? {
        val user = firebaseAuth.currentUser ?: return null
        return UserAccountModel(
            uid = user.uid,
            displayName = user.displayName ?: "",
            email = user.email ?: "",
            photoUrl = user.photoUrl?.toString() ?: "",
            keepSignedIn = isKeepSignedIn(),
        )
    }

    override suspend fun signInWithGoogleCredential(
        idToken: String,
        keepSignedIn: Boolean
    ): Result<UserAccountModel> = withContext(Dispatchers.IO) {
        return@withContext try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user ?: throw Exception("Firebase user is null after sign in")

            setKeepSignedIn(keepSignedIn)

            Result.success(
                UserAccountModel(
                    uid = user.uid,
                    displayName = user.displayName ?: "",
                    email = user.email ?: "",
                    photoUrl = user.photoUrl?.toString() ?: "",
                    keepSignedIn = keepSignedIn,
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            firebaseAuth.signOut()
            setKeepSignedIn(false)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isKeepSignedIn(): Boolean {
        return prefs.getBoolean(KEY_KEEP_SIGNED_IN, false)
    }

    override fun setKeepSignedIn(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_KEEP_SIGNED_IN, enabled) }
    }

    override suspend fun getIdToken(): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val user = firebaseAuth.currentUser ?: throw Exception("User is not signed in")
            val tokenResult = user.getIdToken(true).await()
            val token = tokenResult.token ?: throw Exception("ID token is null")
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
