package ua.turskyi.di

import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ua.turskyi.data.BuildConfig.DATABASE_NAME
import ua.turskyi.data.BuildConfig.HOST_URL
import ua.turskyi.data.api.datasource.CountriesNetSource
import ua.turskyi.data.api.service.CountriesApi
import ua.turskyi.data.hasNetwork
import ua.turskyi.data.repository.CountriesRepositoryImpl
import ua.turskyi.data.room.Database
import ua.turskyi.data.room.datasource.CountriesDbSource
import ua.turskyi.domain.repository.CountriesRepository

val repositoriesModule = module {
    factory<CountriesRepository> { CountriesRepositoryImpl() }
}

val dataProvidersModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            Database::class.java, DATABASE_NAME
        ).build()
    }
    single {
        OkHttpClient.Builder()
            .cache(get<Cache>())
            .addInterceptor { chain ->
                var request = chain.request()
                request = if (hasNetwork(androidContext()))
                    request.newBuilder().header(
                        "Cache-Control",
                        "public, max-age=" + 5
                    ).build()
                else
                    request.newBuilder().header(
                        "Cache-Control",
                        "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7
                    ).build()
                chain.proceed(request)
            }
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }

    single {
        HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT)
            .setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    single<Gson> {
        GsonBuilder()
            .setLenient()
            .create()
    }

    single {
        val cacheSize = (5 * 1024 * 1024).toLong()
        Cache(androidContext().cacheDir, cacheSize)
    }
    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl(HOST_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(GsonConverterFactory.create(get<Gson>())).build()
    }
}

val sourcesModule = module {
    single { get<Database>().genericDao() }
    single { get<Retrofit>().create(CountriesApi::class.java) }
    single { CountriesDbSource(get()) }
    single { CountriesNetSource(get()) }
}

