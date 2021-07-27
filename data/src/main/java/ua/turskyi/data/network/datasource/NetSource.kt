package ua.turskyi.data.network.datasource

import android.accounts.NetworkErrorException
import org.koin.core.component.KoinComponent
import ua.turskyi.data.network.service.CountriesApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ua.turskyi.data.entities.network.CountryListResponse
import ua.turskyi.data.entities.network.CountryNet
import ua.turskyi.data.util.throwException

class NetSource(private val countriesApi: CountriesApi) : KoinComponent {

    fun getCountryNetList(
        onComplete: (List<CountryNet>?) -> Unit,
        onError: (Exception) -> Unit) {
        countriesApi.getCategoriesFromApi().enqueue(object : Callback<CountryListResponse> {
            override fun onFailure(call: Call<CountryListResponse>, t: Throwable) {
                onError(NetworkErrorException(t))
            }

            override fun onResponse(
                call: Call<CountryListResponse>,
                response: Response<CountryListResponse>
            ) {
                if (response.isSuccessful) {
                    onComplete(response.body())
                } else {
                    onError(response.code().throwException(response.message()))
                }
            }
        })
    }
}