package ua.turskyi.travelling.features.home.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ua.turskyi.domain.interactors.CountriesInteractor
import ua.turskyi.travelling.extensions.mapNodeToModel
import ua.turskyi.travelling.models.CityNode

class AddCityViewModel(private val interactor: CountriesInteractor) : ViewModel(){
    fun addCityToCountry(cityNode: CityNode) {
        viewModelScope.launch {
            interactor.insertCity(cityNode.mapNodeToModel())
        }
    }
}