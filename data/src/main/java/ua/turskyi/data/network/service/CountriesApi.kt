package ua.turskyi.data.network.service

import retrofit2.Call
import retrofit2.http.GET
import ua.turskyi.data.entities.network.CountryListResponse

interface CountriesApi {
    companion object {
        const val ENDPOINT_NAME = "rest/v2/all"
    }

    @GET(ENDPOINT_NAME)
    fun getCategoriesFromApi(): Call<CountryListResponse>
}