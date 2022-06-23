package com.org.market

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.view.LayoutInflater
import androidx.lifecycle.MutableLiveData
import androidx.multidex.MultiDex
import com.ActionBar.log
import com.org.ui.components.appInflater


class ApplicationLoader : Application() {
    companion object {
        @Volatile
        lateinit var applicationContext: Context
        @Volatile
        var currentNetworkInfo: NetworkInfo? = null
        @Volatile
        lateinit var applicationHandler: android.os.Handler

        lateinit var connectivityManager: ConnectivityManager

        @Volatile
        var applicationInited = false

        var startTime = 0L

        @Volatile
        var isScreenOn = false

        var connectionObservable = MutableLiveData(false)
        var isConnected = false
        set(value) {
            field = value
            connectionObservable.postValue(value)
        }

        fun applyNetworkInfo() {
            connectivityManager
                .apply {
                    currentNetworkInfo =
                        activeNetworkInfo ?: (getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                            ?: getNetworkInfo(ConnectivityManager.TYPE_WIFI))

                    isConnected = currentNetworkInfo?.isConnectedOrConnecting == true
                }
        }

        fun postInitApplication() {
            if (applicationInited) return
            applicationInited = true

            applicationContext.apply {
                try {
                    connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val receiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context?, intent: Intent?) {
                            applyNetworkInfo()
                        }
                    }
                    registerReceiver(receiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
                } catch (e: java.lang.Exception) {
                    log(e)
                }

                try {
                    val screenBroadcastReceiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context?, intent: Intent) {
                            isScreenOn = when(intent.action) {
                                Intent.ACTION_SCREEN_OFF -> false
                                Intent.ACTION_SCREEN_ON -> true
                                else -> true
                            }
                        }
                    }
                    registerReceiver(
                        screenBroadcastReceiver,
                        IntentFilter().apply {
                            addAction(Intent.ACTION_SCREEN_ON)
                            addAction(Intent.ACTION_SCREEN_OFF)
                        }
                    )
                } catch (e:Exception) {
                    log(e)
                }

                try {
                    isScreenOn = (getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive
                } catch (e: java.lang.Exception) {

                }
                loadMainConfig()
                ShippingController.loadShipping()
                UserConfig.apply {
                    loadConfig()
                    if (hasUser()) {
                        DataCenter.putFullUser(userFull)
                    }
                }
                initImageKit()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            ApplicationLoader.applicationContext = applicationContext
        } catch (e: Exception) {

        }
        log("App start time ${SystemClock.elapsedRealtime().also { startTime = it }}")

        applicationHandler = android.os.Handler(applicationContext.mainLooper)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}
