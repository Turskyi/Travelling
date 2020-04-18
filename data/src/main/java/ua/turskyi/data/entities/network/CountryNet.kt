package ua.turskyi.data.entities.network

import com.google.gson.annotations.SerializedName
import ua.turskyi.domain.model.CityModel

typealias CountryListResponse = MutableList<CountryNet>
data class CountryNet(
    var id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("flag") val flag: String,
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