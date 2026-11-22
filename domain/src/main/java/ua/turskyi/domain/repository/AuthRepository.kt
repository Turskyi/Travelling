package ua.turskyi.domain.repository

import ua.turskyi.domain.model.UserAccountModel

interface AuthRepository {
    fun getCurrentUser(): UserAccountModel?
    suspend fun signInWithGoogleCredential(idToken: String, keepSignedIn: Boolean): Result<UserAccountModel>
    suspend fun signOut(): Result<Unit>
    fun isKeepSignedIn(): Boolean
    fun setKeepSignedIn(enabled: Boolean)
    suspend fun getIdToken(): Result<String>
}
