package ua.turskyi.travelling.features.home.viewmodel

import androidx.lifecycle.ViewModel
import ua.turskyi.domain.interactors.CountriesInteractor
import ua.turskyi.travelling.models.CityNode
import ua.turskyi.travelling.models.CountryNode

class AddCityViewModel(private val interactor: CountriesInteractor) : ViewModel(){
    fun addCityToCountry(countryNode: CountryNode, cityNode: CityNode?){
        cityNode?.let { countryNode.childNode?.add(it) }
    }
}