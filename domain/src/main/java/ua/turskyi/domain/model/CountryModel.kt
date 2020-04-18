package ua.turskyi.domain.model

data class CountryModel(
    var id: Int,
    val name: String,
    val flag: String,
    var visited: Boolean?,
    var cities: MutableList<CityModel>?
) {
    constructor(id: Int, name: String, flag: String, visited: Boolean?) : this(
        id,
        name,
        flag,
        visited,
        null
    )
}
