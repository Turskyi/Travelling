package ua.turskyi.data.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build

fun hasNetwork(context: Context): Boolean {
    if (context.getSystemService(Context.CONNECTIVITY_SERVICE) is ConnectivityManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val connectivityManager: ConnectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network: Network? = connectivityManager.activeNetwork
            return if (network != null) {
                val networkCapabilities: NetworkCapabilities? =
                    connectivityManager.getNetworkCapabilities(network)
                return if (networkCapabilities != null) {
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                            || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                } else false
            } else false
        } else {
            // Initial Value
            var isConnected = false

            val connectivityManager: ConnectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            @Suppress("DEPRECATION")
            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            if (activeNetwork != null && activeNetwork.isConnected) {
                isConnected = true
            }
            return isConnected
        }
    } else {
        return false
    }
}