package ua.turskyi.travelling.extensions

import ua.turskyi.domain.model.CountryModel
import ua.turskyi.travelling.model.Country

fun List<CountryModel>.mapModelListToActualList() = this.mapTo(mutableListOf(), { model ->
    model.mapModelToActual()
})

fun CountryModel.mapModelToActual() = Country(
    id, name, flag, visited
)

fun Country.mapActualToModel() = CountryModel(
    id, name, flag, visited
)
