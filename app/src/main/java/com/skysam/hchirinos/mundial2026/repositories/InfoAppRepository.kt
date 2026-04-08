package com.skysam.hchirinos.mundial2026.repositories

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.skysam.hchirinos.mundial2026.BuildConfig
import com.skysam.hchirinos.mundial2026.common.Constants
import com.skysam.hchirinos.mundial2026.dataclass.InfoApp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Hector Chirinos on 28/09/2023.
 */

@Singleton
class InfoAppRepository @Inject constructor(
 private val firestore: FirebaseFirestore
) {
 private fun getInstance(): CollectionReference {
  return firestore.collection(Constants.INFO_APP)
 }

 fun getInfoApp(): Flow<InfoApp> {
  return callbackFlow {
   val request = getInstance()
    .document(BuildConfig.TOURNAMENT_ID)
    .addSnapshotListener { value, error ->
     if (error != null) {
      Log.w(TAG, "Listen failed.", error)
      return@addSnapshotListener
     }

     if (value != null && value.exists()) {
      val infoApp = InfoApp(
       value.getDouble(Constants.VERSION_CODE)?.toInt() ?: 0,
       value.getString(Constants.VERSION_NAME).orEmpty()
      )
      trySend(infoApp)
     } else {
      Log.d(TAG, "Current data: null")
     }
    }
   awaitClose { request.remove() }
  }
 }
}