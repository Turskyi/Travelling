package ua.turskyi.travelling.utils.extensions

import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel
import ua.turskyi.travelling.models.City
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.models.VisitedCountry

fun List<CountryModel>.mapModelListToCountryList(): MutableList<Country> {
    return this.mapTo(mutableListOf()) { it.mapModelToActual() }
}

fun CountryModel.mapModelToNode(): VisitedCountry {
    return VisitedCountry(id = id, title = name, img = flag, isVisited = isVisited, selfie = selfie)
}

fun List<CountryModel>.mapModelListToNodeList(): MutableList<VisitedCountry> {
    return this.mapTo(mutableListOf()) { model -> model.mapModelToNode() }
}

fun CountryModel.mapModelToActual() = Country(id, name, flag, isVisited, selfie)
fun Country.mapToModel() = CountryModel(id, name, flag, isVisited, selfie)
fun VisitedCountry.mapNodeToActual(): Country {
    return Country(id = id, isVisited = isVisited, name = title, flag = img, selfie = selfie)
}

fun CityModel.mapModelToBaseNode(): City = City(id = id, name = name, parentId = parentId, month = month)
fun City.mapNodeToModel(): CityModel = CityModel(id = id, name = name, parentId = parentId, month = month)
