package com.skysam.hchirinos.mundial2026.common

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.skysam.hchirinos.mundial2026.BuildConfig

/**
 * Created by Hector Chirinos on 13/05/2022.
 */

object CloudMessaging {
    private fun getInstance(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }

    fun subscribeToNotifications() {
        getInstance().subscribeToTopic("results_${BuildConfig.TOURNAMENT_ID}")
            .addOnSuccessListener {
                Log.e("MSG OK", "subscribe")
            }
    }

    fun unsubscribeToNotifications() {
        getInstance().unsubscribeFromTopic("results_${BuildConfig.TOURNAMENT_ID}")
    }
}