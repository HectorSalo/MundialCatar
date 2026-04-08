package com.skysam.hchirinos.mundial2026.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.skysam.hchirinos.mundial2026.common.CloudMessaging
import com.skysam.hchirinos.mundial2026.repositories.Preferences

/**
 * Created by Hector Chirinos on 15/05/2022.
 */

class SettingsViewModel: ViewModel() {
    val notificationActive: LiveData<Boolean> = Preferences.getNotificationStatus().asLiveData()

    suspend fun changeNotificationStatus(status: Boolean) {
        Preferences.changeNotificationStatus(status)
        if (status) CloudMessaging.subscribeToNotifications()
        else CloudMessaging.unsubscribeToNotifications()
    }
}