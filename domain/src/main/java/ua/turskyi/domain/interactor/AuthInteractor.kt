package ua.turskyi.domain.interactor

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ua.turskyi.domain.repository.AuthRepository

class AuthInteractor : KoinComponent {
    private val authRepository: AuthRepository by inject()

    fun getCurrentUser() = authRepository.getCurrentUser()

    suspend fun signInWithGoogleCredential(idToken: String, keepSignedIn: Boolean) =
        authRepository.signInWithGoogleCredential(idToken, keepSignedIn)

    suspend fun signOut() = authRepository.signOut()

    fun setKeepSignedIn(enabled: Boolean) = authRepository.setKeepSignedIn(enabled)
}
