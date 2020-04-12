package ua.turskyi.data.extensions

import ua.turskyi.data.entities.network.CountryNet
import ua.turskyi.data.entities.room.CountryEntity
import ua.turskyi.domain.model.CountryModel

fun List<CountryModel>.mapModelListToEntityList() = mapTo(mutableListOf(), {
    it.mapModelToEntity()
})

fun CountryModel.mapModelToEntity() = CountryEntity(id, name, flag, visited)

fun List<CountryNet>.mapNetListToModelList() = this.mapTo(
    mutableListOf(), {
        it.mapModelToEntity()
    }
)

fun CountryEntity.mapModelToEntity() = CountryModel(id, name, flag, visited)

fun CountryNet.mapModelToEntity() = CountryModel(id, name, flag, visited)

fun List<CountryEntity>.mapEntityListToModelList() = mapTo(
    mutableListOf(), {
        it.mapModelToEntity()
    }
)

