package ua.turskyi.data.extensions

import android.util.Log
import ua.turskyi.data.constants.Constants.LOG

fun log(message: String?) {
    Log.d(LOG, "" + message)
}