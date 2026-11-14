package ua.turskyi.travelling.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ua.turskyi.data.datasources.database.Database
import ua.turskyi.travelling.R
import ua.turskyi.travelling.features.home.view.ui.HomeActivity

class VisitedCountriesWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_visited_countries)

            val intent = Intent(context, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            CoroutineScope(Dispatchers.IO).launch {
                val visitedCount = try {
                    Database.invoke(context).countriesDao().getVisitedCountries().size
                } catch (_: Exception) {
                    0
                }
                views.setTextViewText(R.id.tv_widget_visited_count, visitedCount.toString())
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        fun updateWidget(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, VisitedCountriesWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }

        fun isPinWidgetSupported(context: Context): Boolean {
            return (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) &&
                    AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported
        }

        fun requestPinWidget(context: Context) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                    val myProvider = ComponentName(context, VisitedCountriesWidgetProvider::class.java)
                    appWidgetManager.requestPinAppWidget(myProvider, null, null)
                }
            }
        }
    }
}
