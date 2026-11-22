package ua.turskyi.travelling.common

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ua.turskyi.travelling.common.di.adaptersModule
import ua.turskyi.travelling.common.di.dataProvidersModule
import ua.turskyi.travelling.common.di.interactorsModule
import ua.turskyi.travelling.common.di.repositoriesModule
import ua.turskyi.travelling.common.di.sourcesModule
import ua.turskyi.travelling.common.di.viewModelsModule

class App : Application() {

    override fun onCreate() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate()

        val prefs = getSharedPreferences("ua.turskyi.travelling.auth_prefs", MODE_PRIVATE)
        val keepSignedIn = prefs.getBoolean("key_keep_signed_in", false)
        if (!keepSignedIn) {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        }

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

