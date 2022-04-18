package ua.turskyi.data.entities.network

import com.google.gson.annotations.SerializedName

typealias CountriesResponse = ArrayList<CountryResponse>
/* we do not place all the fields from response to the class,
 because it would a little slowdown the performance during deserialization */
data class CountryResponse(
    @SerializedName("flag")
    val flag: String, // https://flagcdn.com/zw.svg
    @SerializedName("name")
    val name: String, // Zimbabwe
)