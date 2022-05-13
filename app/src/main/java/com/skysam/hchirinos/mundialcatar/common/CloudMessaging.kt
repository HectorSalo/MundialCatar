package com.skysam.hchirinos.mundialcatar.common

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Created by Hector Chirinos on 13/05/2022.
 */

object CloudMessaging {
    private fun getInstance(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }

    fun subscribeToNotifications() {
        getInstance().subscribeToTopic(Constants.TOPIC_ALL_NOTIFICATIONS)
            .addOnSuccessListener {
                Log.e("MSG OK", "subscribe")
            }
    }

    fun unsubscribeToNotifications() {
        getInstance().unsubscribeFromTopic(Constants.TOPIC_ALL_NOTIFICATIONS)
    }
}