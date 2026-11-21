package ua.turskyi.travelling.utils.extensions

import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel
import ua.turskyi.domain.model.TravellerModel
import ua.turskyi.travelling.entities.Traveller
import ua.turskyi.travelling.models.City
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.models.VisitedCountry

fun List<CountryModel>.mapModelListToCountryList(): MutableList<Country> {
    return this.mapTo(mutableListOf()) { it.mapModelToCountry() }
}

fun List<CountryModel>.mapModelListToNodeList(): MutableList<VisitedCountry> {
    return this.mapTo(mutableListOf()) { it.mapModelToNode() }
}

fun CountryModel.mapModelToNode() = VisitedCountry(
    id = id,
    title = name,
    img = flag,
    isVisited = isVisited,
    selfie = selfie,
)

fun CountryModel.mapModelToCountry(): Country {
    return Country(id, name, flag, isVisited, selfie)
}

fun Country.mapToModel() = CountryModel(id = id, name = name, flag = flag, isVisited = isVisited, selfie = filePath)

fun VisitedCountry.mapNodeToActual() = Country(
    id = id,
    name = title,
    flag = img,
    isVisited = isVisited,
    filePath = selfie,
)

fun CityModel.mapModelToBaseNode() = City(id = id, name = name, parentId = parentId, month = month)

fun City.mapNodeToModel() = CityModel(id = id, name = name, parentId = parentId, month = month)

fun List<TravellerModel>.mapModelListToTravellerList(): MutableList<Traveller> {
    return this.mapTo(mutableListOf()) { it.mapModelToTraveller() }
}

fun TravellerModel.mapModelToTraveller(): Traveller {
    return Traveller(
        id = id,
        name = name,
        avatar = avatar,
        counter = counter,
        isVisible = isVisible,
    )
}

