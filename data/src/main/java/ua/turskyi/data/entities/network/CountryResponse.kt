package ua.turskyi.data.entities.network

import com.google.gson.annotations.SerializedName

typealias CountriesResponse = ArrayList<CountryResponse>
/* we do not place all the fields from response to the class,
 because it would a little slowdown the performance during deserialization */
data class CountryResponse(
    @SerializedName("cca2")
    val cca2: String, // AW
    @SerializedName("name")
    val name: NameResponse,
)

data class NameResponse(
    @SerializedName("common")
    val common: String, // Aruba
)
