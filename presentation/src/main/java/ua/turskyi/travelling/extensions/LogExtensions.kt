package ua.turskyi.travelling.extensions

import android.util.Log

inline val <reified T : Any?> T.log: T
    get() {
        Log.d(if (this != null) T::class.java.simpleName else "TAG", this.toString())
        return this@log
    }

/**
 * @see [Log] constants
 */
inline fun <reified T : Any?> T?.log(logType: Int = Log.DEBUG): T? {
    val tag = if (this != null) T::class.java.simpleName else "TAG"
    val message = this.toString()
    when (logType) {
        Log.DEBUG -> Log.d(tag, message)
        Log.ERROR -> Log.e(tag, message)
        Log.WARN -> Log.w(tag, message)
        Log.INFO -> Log.i(tag, message)
        Log.VERBOSE -> Log.v(tag, message)
    }
    return this
}