package com.skysam.hchirinos.mundialcatar.repositories

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.common.Mundial
import com.skysam.hchirinos.mundialcatar.dataclass.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Hector Chirinos on 11/05/2022.
 */


@Singleton
class UsersRepository @Inject constructor(
 private val firestore: FirebaseFirestore,
 @ApplicationContext private val context: Context
) {
 private fun getInstance(): CollectionReference {
  return firestore.collection(context.getString(R.string.path_users))
 }

 fun createUser(user: User) {
  val data = hashMapOf(
   Constants.NAME to user.name,
   Constants.IMAGE to user.image,
   Constants.EMAIL to user.email,
   Constants.POINTS to user.points
  )
  getInstance()
   .document(user.id)
   .set(data)
 }

 fun getUsersByPoints(): Flow<List<User>> {
  return callbackFlow {
   val request =
    getInstance()
    .orderBy(Constants.POINTS, Query.Direction.DESCENDING)
    .addSnapshotListener { value, error ->
     if (error != null || value == null) {
      Log.w(ContentValues.TAG, "Listen failed.", error)
      return@addSnapshotListener
     }

     val users = value.documents.mapNotNull { doc ->
      val name = doc.getString(Constants.NAME) ?: return@mapNotNull null
      val image = doc.getString(Constants.IMAGE) ?: ""
      val email = doc.getString(Constants.EMAIL) ?: ""
      val points = doc.getDouble(Constants.POINTS)?.toInt() ?: 0

      User(
       id = doc.id,
       name = name,
       image = image,
       email = email,
       points = points
      )
     }
     trySend(users)
    }
   awaitClose { request.remove() }
  }
 }

 fun updateAllPoints(points: Double, id: String) {
  getInstance()
   .document(id)
   .update(Constants.POINTS, FieldValue.increment(points))
 }
}