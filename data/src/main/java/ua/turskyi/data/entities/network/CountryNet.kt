package ua.turskyi.data.entities.network

import com.google.gson.annotations.SerializedName

typealias CountryListResponse = MutableList<CountryNet>
data class CountryNet(
    @SerializedName("name") val name: String,
    @SerializedName("flag") val flag: String,
)