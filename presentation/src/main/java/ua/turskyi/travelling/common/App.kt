package ua.turskyi.travelling.common

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.facebook.appevents.AppEventsLogger
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ua.turskyi.di.dataProvidersModule
import ua.turskyi.di.managersModule
import ua.turskyi.di.repositoriesModule
import ua.turskyi.di.sourcesModule
import ua.turskyi.travelling.common.di.adaptersModule
import ua.turskyi.travelling.common.di.interactorsModule
import ua.turskyi.travelling.common.di.viewModelsModule

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
                    managersModule
                )
            )
        }
        AppEventsLogger.activateApp(this)
    }
}