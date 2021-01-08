package ua.turskyi.data

import android.content.Context
import android.content.SharedPreferences

class Prefs (context: Context) {
    companion object {
        const val PREFS_FILENAME = "ua.turskyi.travelling.prefs"
        const val IS_UPGRADED = "IS_UPGRADED"
        const val IS_SYNC = "IS_SYNC"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    var isUpgraded: Boolean
        get() = prefs.getBoolean(IS_UPGRADED, false)
        set(value) = prefs.edit().putBoolean(IS_UPGRADED, value).apply()

    var isSynchronized: Boolean
        get() = prefs.getBoolean(IS_SYNC, false)
        set(value) = prefs.edit().putBoolean(IS_SYNC, value).apply()
}