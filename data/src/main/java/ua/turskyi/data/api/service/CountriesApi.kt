package ua.turskyi.data.api.service

import retrofit2.Call
import retrofit2.http.GET
import ua.turskyi.data.constant.ApiEndpoint
import ua.turskyi.data.entities.network.CountryListResponse
import ua.turskyi.data.entities.network.CountryNet

interface CountriesApi {
    @GET(ApiEndpoint.ENDPOINT_NAME)
    fun getCategoriesFromApi(): Call<CountryListResponse>
}