package ua.turskyi.data.entities.network

import com.google.gson.annotations.SerializedName

data class CurrencyResponse(
        @SerializedName("code")
        val code: String, // ZMW
        @SerializedName("name")
        val name: String, // Zambian kwacha
        @SerializedName("symbol")
        val symbol: String // K
    )