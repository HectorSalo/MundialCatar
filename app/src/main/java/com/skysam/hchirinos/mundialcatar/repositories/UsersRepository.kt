package com.skysam.hchirinos.mundialcatar.repositories

import android.content.ContentValues
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.common.Mundial
import com.skysam.hchirinos.mundialcatar.dataclass.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Created by Hector Chirinos on 11/05/2022.
 */

object UsersRepository {
 private fun getInstance(): CollectionReference {
  return FirebaseFirestore.getInstance().collection(Mundial.Mundial.getContext().getString(R.string.path_users))
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
    //FirebaseFirestore.getInstance().collection("usersCampeones")
    .orderBy(Constants.POINTS, Query.Direction.DESCENDING)
    .addSnapshotListener { value, error ->
     if (error != null) {
      Log.w(ContentValues.TAG, "Listen failed.", error)
      return@addSnapshotListener
     }

     val users = mutableListOf<User>()
     for (user in value!!) {
      val newUser = User(
       user.id,
       user.getString(Constants.NAME)!!,
       user.getString(Constants.IMAGE)!!,
       user.getString(Constants.EMAIL)!!,
       user.getDouble(Constants.POINTS)!!.toInt()
      )
      users.add(newUser)
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