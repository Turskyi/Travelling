package ua.turskyi.travelling.common

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ua.turskyi.travelling.common.di.*

class App : Application() {

    override fun onCreate() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate()

        startKoin {
            androidContext(applicationContext)
            modules(
                listOf(
                    sourcesModule,
                    dataProvidersModule,
                    adaptersModule,
                    viewModelsModule,
                    interactorsModule,
                    repositoriesModule,
                )
            )
        }
    }
}