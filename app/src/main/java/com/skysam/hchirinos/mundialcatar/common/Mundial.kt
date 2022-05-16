package com.skysam.hchirinos.mundialcatar.common

import android.app.Application
import android.content.Context
import androidx.lifecycle.asLiveData
import com.skysam.hchirinos.mundialcatar.repositories.Preferences

/**
 * Created by Hector Chirinos on 15/05/2022.
 */

class Mundial: Application() {
    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        Preferences.getNotificationStatus().asLiveData().observeForever {
            if (it) CloudMessaging.subscribeToNotifications()
            else CloudMessaging.unsubscribeToNotifications()
        }
    }

    object Mundial {
        fun getContext(): Context = appContext
    }
}