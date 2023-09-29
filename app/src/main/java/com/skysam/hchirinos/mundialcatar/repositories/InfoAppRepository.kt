package com.skysam.hchirinos.mundialcatar.repositories

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.common.Mundial
import com.skysam.hchirinos.mundialcatar.dataclass.InfoApp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Created by Hector Chirinos on 28/09/2023.
 */

object InfoAppRepository {
 private fun getInstance(): CollectionReference {
  return FirebaseFirestore.getInstance().collection(Constants.INFO_APP)
 }

 fun getInfoApp(): Flow<InfoApp> {
  return callbackFlow {
   val request = getInstance()
    .document(Mundial.Mundial.getContext().getString(R.string.info_app))
    .addSnapshotListener { value, error ->
     if (error != null) {
      Log.w(TAG, "Listen failed.", error)
      return@addSnapshotListener
     }

     if (value != null && value.exists()) {
      val infoApp = InfoApp(
       value.getDouble(Constants.VERSION_CODE)!!.toInt(),
       value.getString(Constants.VERSION_NAME)!!
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