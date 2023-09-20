package com.skysam.hchirinos.mundialcatar.common

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.skysam.hchirinos.mundialcatar.R

/**
 * Created by Hector Chirinos on 13/05/2022.
 */

object CloudMessaging {
    private fun getInstance(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }

    fun subscribeToNotifications() {
        getInstance().subscribeToTopic(Mundial.Mundial.getContext().getString(R.string.notification_topic))
            .addOnSuccessListener {
                Log.e("MSG OK", "subscribe")
            }
    }

    fun unsubscribeToNotifications() {
        getInstance().unsubscribeFromTopic(Mundial.Mundial.getContext().getString(R.string.notification_topic))
    }
}