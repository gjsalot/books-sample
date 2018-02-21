package com.gjsalot.books.utils

import android.arch.lifecycle.LiveData
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.gjsalot.books.app.Application

object ConnectivityLiveData: LiveData<Boolean>() {

    private var broadcastReceiver: BroadcastReceiver? = null
    lateinit var application: Application

    fun init(application: Application) {
        this.application = application
    }

    override fun onActive() {
        registerBroadcastReceiver()
    }

    override fun onInactive() {
        unregisterBroadcastReceiver()
    }

    private fun registerBroadcastReceiver() {
        if (broadcastReceiver == null) {
            val filter = IntentFilter()
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)

            broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(_context: Context, intent: Intent) {
                    val extras = intent.extras
                    val info = extras.getParcelable<NetworkInfo>("networkInfo")
                    value = info.state == NetworkInfo.State.CONNECTED
                }
            }

            application.registerReceiver(broadcastReceiver, filter)
        }
    }

    private fun unregisterBroadcastReceiver() {
        if (broadcastReceiver != null) {
            application.unregisterReceiver(broadcastReceiver)
            broadcastReceiver = null
        }
    }
}
