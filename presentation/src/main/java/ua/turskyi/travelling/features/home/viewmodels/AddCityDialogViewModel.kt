package ua.turskyi.travelling.features.home.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ua.turskyi.domain.interactor.CountriesInteractor
import ua.turskyi.travelling.models.City
import ua.turskyi.travelling.utils.extensions.mapNodeToModel

class AddCityDialogViewModel(private val interactor: CountriesInteractor) : ViewModel() {
    fun insert(
        city: City,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit,
    ) {
        viewModelScope.launch {
            interactor.insertCity(city.mapNodeToModel(), onSuccess = onSuccess, onError = onError)
        }
    }
}