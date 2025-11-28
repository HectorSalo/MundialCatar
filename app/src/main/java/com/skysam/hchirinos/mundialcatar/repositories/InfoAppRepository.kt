package com.skysam.hchirinos.mundialcatar.repositories

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.common.Mundial
import com.skysam.hchirinos.mundialcatar.dataclass.InfoApp
import dagger.hilt.android.qualifiers.ApplicationContext
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
 private val firestore: FirebaseFirestore,
 @ApplicationContext private val context: Context
) {
 private fun getInstance(): CollectionReference {
  return firestore.collection(Constants.INFO_APP)
 }

 fun getInfoApp(): Flow<InfoApp> {
  return callbackFlow {
   val request = getInstance()
    .document(context.getString(R.string.info_app))
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