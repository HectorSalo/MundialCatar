package com.skysam.hchirinos.mundialcatar.repositories

import android.content.ContentValues
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skysam.hchirinos.mundialcatar.BuildConfig
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.common.Mundial
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameUser
import com.skysam.hchirinos.mundialcatar.dataclass.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Created by Hector Chirinos on 11/05/2022.
 */

object UsersRepository {
 private val GROUPS = arrayOf(
  Constants.GROUP_A,
  Constants.GROUP_B,
  Constants.GROUP_C,
  Constants.GROUP_D,
  Constants.GROUP_E,
  Constants.GROUP_F,
  Constants.GROUP_G,
  Constants.GROUP_H
 )

 private fun getInstance(): CollectionReference {
  return FirebaseFirestore.getInstance().collection(Mundial.Mundial.getContext().getString(R.string.path_users))
 }

 fun createUser(user: User) {
  val data = hashMapOf(
   Constants.NAME to user.name,
   Constants.IMAGE to user.image,
   Constants.EMAIL to user.email
  )
  getInstance()
   .document(user.id)
   .set(data)
 }

 fun getUsersByPoints(): Flow<List<User>> {
  return callbackFlow {
   val request = getInstance()
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
       user.getDouble(Constants.POINTS)!!.toInt(),
       mutableListOf()
      )
      users.add(newUser)
     }
     trySend(users)
    }
   awaitClose { request.remove() }
  }
 }

 fun updatePredictGroups(game: GameUser) {
  val data: Map<String, Any> = hashMapOf(
   Constants.GOALS1 to game.goalsTeam1,
   Constants.GOALS2 to game.goalsTeam2
  )
  /*getInstanceGameUser()
   .document(game.id)
   .update(data)*/
 }

 fun updatePointsByGame(game: Game, users: MutableList<User>) {
  /*for (user in users) {
   getInstanceGameUserById(user.id)
    .document(game.id)
    .get().addOnSuccessListener {
     if (it != null) {
      var points = 0
      val goals1 = it.getDouble(Constants.GOALS1)?.toInt()
      val goals2 = it.getDouble(Constants.GOALS2)?.toInt()

      if (goals1 != null && goals2 != null) {
       if (goals1 == game.goalsTeam1 && goals2 == game.goalsTeam2) {
        points = 3
       }
       if ((goals1 > goals2) && (game.goalsTeam1 > game.goalsTeam2) && (goals1 != game.goalsTeam1 || goals2 != game.goalsTeam2)) {
        points = 2
       }
       if ((goals1 < goals2) && (game.goalsTeam1 < game.goalsTeam2) && (goals1 != game.goalsTeam1 || goals2 != game.goalsTeam2)) {
        points = 2
       }
       if (goals1 == goals2 && game.goalsTeam1 == game.goalsTeam2 && game.goalsTeam1 != goals1)
        points = 2
       if (goals1 > goals2 && game.goalsTeam1 < game.goalsTeam2 && (goals1 != game.goalsTeam1 || goals2 != game.goalsTeam2))
        points = -1
       if (goals1 < goals2 && game.goalsTeam1 > game.goalsTeam2 && (goals1 != game.goalsTeam1 || goals2 != game.goalsTeam2))
        points = -1
       if ((goals1 > goals2 || goals1 < goals2) && game.goalsTeam1 == game.goalsTeam2)
        points = -1
       if ((game.goalsTeam1 < game.goalsTeam2 || game.goalsTeam1 > game.goalsTeam2) && goals1 == goals2)
        points = -1
       if (goals1 == game.goalsTeam2 && goals2 == game.goalsTeam1 && game.goalsTeam1 != game.goalsTeam2)
        points = -2

       getInstanceGameUserById(user.id)
        .document(game.id)
        .update(Constants.POINTS, points.toDouble())
        .addOnSuccessListener { updateAllPoints(points.toDouble(), user.id) }
      }
     }
    }

  }*/
 }

 fun updatePointsByGamePlayOff(game: Game, users: MutableList<User>) {
  /*for (user in users) {
   getInstanceGameUserById(user.id)
    .document(game.id)
    .get().addOnSuccessListener {
     if (it != null) {
      var points = 0
      val goals1 = it.getDouble(Constants.GOALS1)?.toInt()
      val goals2 = it.getDouble(Constants.GOALS2)?.toInt()
      val penal1 = it.getDouble(Constants.PENAL1)?.toInt()
      val penal2 = it.getDouble(Constants.PENAL2)?.toInt()

      if (goals1 != null && goals2 != null) {
       if (goals1 == game.goalsTeam1 && goals2 == game.goalsTeam2 && game.goalsTeam1 != game.goalsTeam2) {
        points = 3
       }
       if ((goals1 > goals2) && (game.goalsTeam1 > game.goalsTeam2) && (goals1 != game.goalsTeam1 || goals2 != game.goalsTeam2)) {
        points = 2
       }
       if ((goals1 < goals2) && (game.goalsTeam1 < game.goalsTeam2) && (goals1 != game.goalsTeam1 || goals2 != game.goalsTeam2)) {
        points = 2
       }
       if (goals1 > goals2 && game.goalsTeam1 < game.goalsTeam2 && (goals1 != game.goalsTeam1 || goals2 != game.goalsTeam2))
        points = -1
       if (goals1 < goals2 && game.goalsTeam1 > game.goalsTeam2 && (goals1 != game.goalsTeam1 || goals2 != game.goalsTeam2))
        points = -1
       if (goals1 == game.goalsTeam2 && goals2 == game.goalsTeam1 && game.goalsTeam1 != game.goalsTeam2)
        points = -2

       if (goals1 == goals2) {
        if (game.goalsTeam1 < game.goalsTeam2 && penal1!! < penal2!!)
         points = 1
        if (game.goalsTeam1 < game.goalsTeam2 && penal1!! > penal2!!)
         points = -1
        if (game.goalsTeam1 > game.goalsTeam2 && penal1!! > penal2!!)
         points = 1
        if (game.goalsTeam1 > game.goalsTeam2 && penal1!! < penal2!!)
         points = -1
       }

       if (game.goalsTeam1 == game.goalsTeam2) {
        if (goals1 > goals2 && game.penal1 > game.penal2)
         points = 1
        if (goals1 < goals2 && game.penal1 > game.penal2)
         points = -1
        if (goals1 < goals2 && game.penal1 < game.penal2)
         points = 1
        if (goals1 > goals2 && game.penal1 < game.penal2)
         points = -1
       }

       if (game.goalsTeam1 == game.goalsTeam2 && goals1 == goals2) {
        if (game.goalsTeam1 == goals1) {
         if (penal1!! < penal2!! && game.penal1 < game.penal2)
          points = 4
         if (penal1 > penal2 && game.penal1 > game.penal2)
          points = 4
         if (penal1 < penal2 && game.penal1 > game.penal2)
          points = 2
         if (penal1 > penal2 && game.penal1 < game.penal2)
          points = 2
        }

        if (game.goalsTeam1 != goals1) {
         if (penal1!! < penal2!! && game.penal1 < game.penal2)
          points = 3
         if (penal1 > penal2 && game.penal1 > game.penal2)
          points = 3
         if (penal1 < penal2 && game.penal1 > game.penal2)
          points = 1
         if (penal1 > penal2 && game.penal1 < game.penal2)
          points = 1
        }
       }

       getInstanceGameUserById(user.id)
        .document(game.id)
        .update(Constants.POINTS, points.toDouble())
        .addOnSuccessListener { updateAllPoints(points.toDouble(), user.id) }
      }
     }
    }
  }*/
 }

 private fun updateAllPoints(points: Double, id: String) {
  getInstance()
   .document(id)
   .update(Constants.POINTS, FieldValue.increment(points))
 }

 fun updateTimeGame(games: MutableList<Game>, users: MutableList<User>) {
  /*val calendarCurrent = Calendar.getInstance()
  calendarCurrent.set(Calendar.DAY_OF_MONTH, 6)
  calendarCurrent.set(Calendar.MINUTE, 0)
  calendarCurrent.set(Calendar.MONTH, 11)
  calendarCurrent.set(Calendar.HOUR_OF_DAY, 15)
  for (user in users) {
   val data: Map<String, Any> = hashMapOf(
    Constants.DATE to calendarCurrent.time
   )
   getInstanceGameUserById(user.id)
    .document("game56")
    .update(data)
  }*/
 }
}