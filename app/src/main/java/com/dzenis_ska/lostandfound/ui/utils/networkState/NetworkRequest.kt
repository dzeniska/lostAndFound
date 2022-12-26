package com.dzenis_ska.lostandfound.ui.utils.networkState

import android.annotation.SuppressLint
import android.content.Context
import android.net.*
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

typealias OnAvailable = () -> Unit

class NetworkRequest(val context: Context): ConnectivityManager.NetworkCallback() {

    var onAvailable: OnAvailable? = null

    var connectivityManager: ConnectivityManager? = null

        override fun onAvailable(network : Network) {
            onAvailable?.let { it() }
            Log.d("!!!networkCallback", "The default network is now: " + network)
        }

        override fun onLost(network : Network) {
            Log.d("!!!networkCallback", "The application no longer has a default network. The last default network was " + network)
        }

        override fun onCapabilitiesChanged(network : Network, networkCapabilities : NetworkCapabilities) {
            Log.d("!!!networkCallback", "The default network changed capabilities: " + networkCapabilities)
        }

        override fun onLinkPropertiesChanged(network : Network, linkProperties : LinkProperties) {
            Log.d("!!!networkCallback", "The default network changed link properties: " + linkProperties.linkAddresses)
        }

    @SuppressLint("MissingPermission")
    fun isOnline(): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connMgr.activeNetworkInfo
        return networkInfo?.isConnected == true
    }

    @SuppressLint("MissingPermission")
    fun networkCallback(onAvailable:OnAvailable){
        this.onAvailable = onAvailable
        if (connectivityManager != null) return
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager?.registerDefaultNetworkCallback(this)
        }
    }

    fun connectivityManager() = connectivityManager

    fun unregisterNetworkCallback(){
        try {
            connectivityManager?.unregisterNetworkCallback(this)
            connectivityManager = null
        } catch (e: Exception){
            Log.d("!!!networkCallbackException", "The default network changed link properties: " + e.message)
        }
    }
}
