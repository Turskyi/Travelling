package ua.turskyi.travelling.features.account.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ua.turskyi.domain.interactor.AuthInteractor
import ua.turskyi.domain.model.UserAccountModel

class AccountActivityViewModel : ViewModel(), KoinComponent {

    private val authInteractor: AuthInteractor by inject()

    private val _userAccount = MutableLiveData<UserAccountModel?>()
    val userAccount: LiveData<UserAccountModel?> = _userAccount

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    init {
        loadUser()
    }

    fun loadUser() {
        _userAccount.value = authInteractor.getCurrentUser()
    }

    fun setKeepSignedIn(enabled: Boolean) {
        authInteractor.setKeepSignedIn(enabled)
    }

    fun signOut() {
        viewModelScope.launch {
            authInteractor.signOut()
            loadUser()
        }
    }
}
