package ua.turskyi.data.entities.network

import com.google.gson.annotations.SerializedName

data class RegionalBlocResponse(
    @SerializedName("acronym")
    val acronym: String, // AU
    @SerializedName("name")
    val name: String, // African Union
    @SerializedName("otherNames")
    val otherNames: List<String>
)