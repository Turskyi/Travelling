package ua.turskyi.data.network.service

import retrofit2.Call
import retrofit2.http.GET
import ua.turskyi.data.constants.ApiEndpoint
import ua.turskyi.data.entities.network.CountryListResponse

interface CountriesApi {
    @GET(ApiEndpoint.ENDPOINT_NAME)
    fun getCategoriesFromApi(): Call<CountryListResponse>
}