package ua.turskyi.domain.model

data class CityModel(
    var id: Int?,
    val name: String,
    var parentId: Int
)
