package ua.turskyi.data.extensions

import ua.turskyi.data.entities.network.CountryNet
import ua.turskyi.data.entities.room.CityEntity
import ua.turskyi.data.entities.room.CountryEntity
import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel

fun List<CountryModel>.mapModelListToEntityList() =
    mapTo(mutableListOf(), { countryModel -> countryModel.mapModelToEntity() })

fun CountryModel.mapModelToEntity() = CountryEntity(id, name, flag, visited)
fun CityModel.mapModelToEntity() = CityEntity(id = id, name =  name, parentId = parentId)
fun CityEntity.mapEntityToModel() = CityModel(id = id, name = name, parentId = parentId)
fun List<CountryNet>.mapNetListToModelList() = this.mapTo(
    mutableListOf(), { countryNet -> countryNet.mapNetToEntity() })

fun CountryEntity.mapEntityToModel() = CountryModel(id, name, flag, visited)
fun CountryNet.mapNetToEntity() = CountryModel(id, name, flag, visited)
fun MutableList<CityEntity>.mapEntitiesToModelList() = mapTo(
    mutableListOf(), { cityEntity -> cityEntity.mapEntityToModel() })

fun List<CountryEntity>.mapEntityListToModelList() = mapTo(
    mutableListOf(), { countryEntity -> countryEntity.mapEntityToModel() })

