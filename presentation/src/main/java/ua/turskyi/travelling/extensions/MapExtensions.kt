package ua.turskyi.travelling.extensions

import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel
import ua.turskyi.travelling.models.City
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.models.VisitedCountry

fun List<CountryModel>.mapModelListToActualList() = this.mapTo(
    mutableListOf(), { model -> model.mapModelToActual() })

fun List<CountryModel>.mapModelListToNodeList() = this.mapTo(
    mutableListOf(), { model -> model.mapModelToNode() })

fun CountryModel.mapModelToNode() = VisitedCountry(
    id = id, title = name, img = flag, visited = visited, selfie = selfie
)

fun CountryModel.mapModelToActual() = Country(id, name, flag, visited, selfie)
fun Country.mapActualToModel() = CountryModel(id, name, flag, visited, selfie)
fun VisitedCountry.mapNodeToActual() = Country(
    id = id, visited = visited, name = title,
    flag = img, selfie = selfie
)

fun CityModel.mapModelToBaseNode() = City(id = id, name = name, parentId = parentId)
fun City.mapNodeToModel() = CityModel(id = id, name = name, parentId = parentId)
