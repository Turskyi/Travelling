package ua.turskyi.travelling.common

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.facebook.appevents.AppEventsLogger
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ua.turskyi.di.dataProvidersModule
import ua.turskyi.di.repositoriesModule
import ua.turskyi.di.sourcesModule
import ua.turskyi.travelling.common.di.adaptersModule
import ua.turskyi.travelling.common.di.interactorsModule
import ua.turskyi.travelling.common.di.viewModelsModule
import ua.turskyi.travelling.utils.ContextUtil
val prefs: Prefs by lazy {
    App.prefs!!
}

class App : Application() {
    companion object {
        var prefs: Prefs? = null
    }
    override fun onCreate() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        prefs = Prefs(applicationContext)
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
                    repositoriesModule
                )
            )
        }
        ContextUtil.init(this)
        AppEventsLogger.activateApp(this)
    }
}