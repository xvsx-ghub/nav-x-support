package com.wiswm.nav.support.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log

lateinit var connectivityObserverContext: Context

actual class ConnectivityObserver actual constructor(){
    companion object{
        const val TAG = "AndroidConnectivityObserver"
    }

    actual fun create(onConnectionStateChanged: (onlineStatus: Boolean)-> Unit) {
        val cm: ConnectivityManager =
            connectivityObserverContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: throw IllegalStateException("ConnectivityManager service is not available")

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.e(TAG, "ConnectivityManager.NetworkCallback(): onAvailable")
                onConnectionStateChanged(true)
            }

            override fun onLost(network: Network) {
                Log.e(TAG, "ConnectivityManager.NetworkCallback(): onLost")
                onConnectionStateChanged(false)
            }

            override fun onUnavailable() {
                Log.e(TAG, "ConnectivityManager.NetworkCallback(): onUnavailable")
                onConnectionStateChanged(false)
            }
        }

        cm.registerDefaultNetworkCallback(callback)
    }

    private fun isCurrentlyOnline(cm: ConnectivityManager): Boolean {
        return try {
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            false
        }
    }
}
