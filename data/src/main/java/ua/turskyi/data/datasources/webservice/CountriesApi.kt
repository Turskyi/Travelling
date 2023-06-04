package ua.turskyi.data.datasources.webservice

import retrofit2.Call
import retrofit2.http.GET
import ua.turskyi.data.entities.network.CountriesResponse

interface CountriesApi {
    companion object {
        const val ENDPOINT_NAME = "all"
    }

    @GET(ENDPOINT_NAME)
    fun getCategoriesFromApi(): Call<CountriesResponse>
}