package ua.turskyi.travelling.common.di

import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ua.turskyi.data.BuildConfig.DATABASE_NAME
import ua.turskyi.data.BuildConfig.HOST_URL
import ua.turskyi.data.datasources.database.Database
import ua.turskyi.data.datasources.database.datasource.DatabaseSource
import ua.turskyi.data.datasources.webservice.CountriesApi
import ua.turskyi.data.datasources.webservice.NetSource
import ua.turskyi.data.repository.CountriesRepositoryImpl
import ua.turskyi.data.util.hasNetwork
import ua.turskyi.domain.interactor.CountriesInteractor
import ua.turskyi.domain.repository.CountriesRepository
import ua.turskyi.travelling.features.allcountries.view.adapter.AllCountriesAdapter
import ua.turskyi.travelling.features.allcountries.viewmodel.AllCountriesActivityViewModel
import ua.turskyi.travelling.features.flags.view.adapter.FlagsAdapter
import ua.turskyi.travelling.features.flags.viewmodel.FlagsFragmentViewModel
import ua.turskyi.travelling.features.home.view.adapter.HomeAdapter
import ua.turskyi.travelling.features.home.viewmodels.AddCityDialogViewModel
import ua.turskyi.travelling.features.home.viewmodels.HomeActivityViewModel

// top (outer) level dependency injections
val adaptersModule = module {
    factory { HomeAdapter() }
    factory { AllCountriesAdapter() }
    factory { FlagsAdapter(get()) }
}

val viewModelsModule = module {
    factory { HomeActivityViewModel(get()) }
    factory { AllCountriesActivityViewModel(get()) }
    factory { FlagsFragmentViewModel(get()) }
    factory { AddCityDialogViewModel(get()) }
}

val interactorsModule = module {
    factory { CountriesInteractor() }
}

// low (inner) level dependency injections

val repositoriesModule = module {
    factory<CountriesRepository> { CountriesRepositoryImpl(CoroutineScope(SupervisorJob())) }
}

val dataProvidersModule = module {
    single { Room.databaseBuilder(androidContext(), Database::class.java, DATABASE_NAME).build() }
    single {
        OkHttpClient.Builder()
            .cache(get())
            .addInterceptor { chain ->
                var request: Request = chain.request()
                request = if (hasNetwork(androidContext())) {
                    request.newBuilder().header(
                        "Cache-Control",
                        "public, max-age=" + 5
                    ).build()
                } else {
                    request.newBuilder().header(
                        "Cache-Control",
                        "public, only-if-cached, max-stale=" + (60 * 60 * 24 * 7)
                    ).build()
                }
                chain.proceed(request)
            }.build()
    }

    single<Gson> { GsonBuilder().setLenient().create() }

    single {
        val cacheSize: Long = (5 * 1024 * 1024).toLong()
        Cache(androidContext().cacheDir, cacheSize)
    }
    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl(HOST_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(get())).build()
    }
}

val sourcesModule = module {
    single { get<Database>().countriesDao() }
    single { get<Retrofit>().create(CountriesApi::class.java) }
    single { DatabaseSource(get()) }
    single { NetSource(get()) }
}



