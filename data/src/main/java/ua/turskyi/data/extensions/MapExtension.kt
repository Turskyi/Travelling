package ua.turskyi.data.extensions

import ua.turskyi.data.entities.network.CountryNet
import ua.turskyi.data.entities.room.CityEntity
import ua.turskyi.data.entities.room.CountryEntity
import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel

fun List<CountryModel>.mapModelListToEntityList() =
    mapTo(mutableListOf(), { countryModel -> countryModel.mapModelToEntity() })

fun CountryModel.mapModelToEntity() = CountryEntity(id, name, flag, visited, null)
fun CityModel.mapModelToEntity() =
    CityEntity(id = id, name = name, parentId = parentId, month = month)

fun CityEntity.mapEntityToModel() =
    CityModel(id = id, name = name, parentId = parentId, month = month)

fun List<CountryNet>.mapNetListToModelList() = this.mapTo(
    mutableListOf(), { countryNet -> countryNet.mapNetToEntity() })

fun CountryEntity.mapEntityToModel() = CountryModel(id, name, flag, visited, selfie)
fun CountryNet.mapNetToEntity() = CountryModel(id, name, flag, visited, selfie = null)
fun MutableList<CityEntity>.mapEntitiesToModelList() = mapTo(
    mutableListOf(), { cityEntity -> cityEntity.mapEntityToModel() })

fun List<CountryEntity>.mapEntityListToModelList() = mapTo(
    mutableListOf(), { countryEntity -> countryEntity.mapEntityToModel() })

