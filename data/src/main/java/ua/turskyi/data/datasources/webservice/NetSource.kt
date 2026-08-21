package ua.turskyi.data.datasources.webservice

import org.koin.core.component.KoinComponent
import retrofit2.Response
import ua.turskyi.data.entities.network.CountriesResponse
import ua.turskyi.data.entities.network.CountryResponse
import ua.turskyi.data.util.throwException

class NetSource(private val countriesApi: CountriesApi) : KoinComponent {

    fun getCountryNetList(
        onComplete: (List<CountryResponse>?) -> Unit,
        onError: (Exception) -> Unit,
    ) {
        try {
            val response: Response<CountriesResponse> = countriesApi.getCategoriesFromApi().execute()
            if (response.isSuccessful) {
                onComplete(response.body())
            } else {
                onError(response.code().throwException(response.message()))
            }
        } catch (exception: Exception) {
            onError(exception)
        }
    }
}
