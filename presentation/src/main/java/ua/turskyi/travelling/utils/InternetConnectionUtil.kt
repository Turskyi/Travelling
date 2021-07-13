package ua.turskyi.travelling.utils

import java.io.IOException

/**
 * @Description
 * Checks if device is online or not
 */
fun isOnline(): Boolean {
    val runtime: Runtime = Runtime.getRuntime()
    try {
        val ipProcess: Process = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
        val exitValue: Int = ipProcess.waitFor()
        return exitValue == 0
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }
    return false
}