package ua.turskyi.data.extensions

import ua.turskyi.data.entities.network.CountryResponse
import ua.turskyi.data.entities.local.CityEntity
import ua.turskyi.data.entities.local.CountryEntity
import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel

fun List<CountryModel>.mapModelListToEntityList(): MutableList<CountryEntity> {
    return mapTo(mutableListOf()) { countryModel -> countryModel.mapModelToEntity() }
}

fun CountryModel.mapModelToEntity(): CountryEntity = CountryEntity(id, name, flag, isVisited, "")
fun CityModel.mapModelToEntity(): CityEntity {
    return CityEntity(id = id, name = name, parentId = parentId, month = month)
}

fun CityEntity.mapEntityToModel(): CityModel {
    return CityModel(id = id, name = name, parentId = parentId, month = month)
}

fun List<CountryResponse>.mapNetListToModelList(): MutableList<CountryModel> {
    return this.mapTo(mutableListOf()) { countryNet: CountryResponse -> countryNet.mapNetToEntity() }
}

fun CountryEntity.mapEntityToModel() = CountryModel(id, name, flag, isVisited, selfie)
fun CountryResponse.mapNetToEntity() = CountryModel(name, flag)
fun List<CityEntity>.mapEntitiesToModelList(): MutableList<CityModel> {
    return mapTo(mutableListOf()) { cityEntity: CityEntity -> cityEntity.mapEntityToModel() }
}

fun List<CountryEntity>.mapEntityListToModelList(): MutableList<CountryModel> {
    return mapTo(mutableListOf()) { countryEntity: CountryEntity -> countryEntity.mapEntityToModel() }
}

