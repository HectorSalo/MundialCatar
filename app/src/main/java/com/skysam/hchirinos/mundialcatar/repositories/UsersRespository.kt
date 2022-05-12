package com.skysam.hchirinos.mundialcatar.repositories

import android.content.ContentValues
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.dataclass.GameUser
import com.skysam.hchirinos.mundialcatar.dataclass.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Created by Hector Chirinos on 11/05/2022.
 */

object UsersRespository {
 private fun getInstance(): CollectionReference {
  return FirebaseFirestore.getInstance().collection(Constants.USERS)
 }

 private fun getInstanceGameUser(id: String): CollectionReference {
  return FirebaseFirestore.getInstance().collection(Constants.USERS)
   .document(id).collection(Constants.GAMES)
 }

 fun createUser(user: User) {
  val data = hashMapOf(
   Constants.NAME to user.name,
   Constants.IMAGE to user.image,
   Constants.EMAIL to user.email,
   Constants.POINTS to user.points
  )
  getInstance().document(user.id).set(data)
   .addOnSuccessListener {
    for (game in user.games) {
     val data2 = hashMapOf(
      Constants.TEAM1 to game.team1,
      Constants.TEAM2 to game.team2,
      Constants.FLAG1 to game.flag1,
      Constants.FLAG2 to game.flag2,
      Constants.DATE to game.date,
      Constants.GOALS1 to game.goalsTeam1,
      Constants.GOALS2 to game.goalsTeam2,
      Constants.PENAL1 to game.penal1,
      Constants.PENAL2 to game.penal2,
      Constants.ROUND to game.round,
      Constants.NUMBER to game.number,
      Constants.POINTS to game.points,
     )
     getInstanceGameUser(user.id).document(game.id)
      .set(data2)
    }
   }
 }

 fun getUsersByPoints(): Flow<MutableList<User>> {
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

 fun getAllGames(): Flow<MutableList<GameUser>> {
  return callbackFlow {
   val request = getInstanceGameUser(Auth.getCurrenUser()!!.uid)
    .orderBy(Constants.DATE, Query.Direction.ASCENDING)
    .addSnapshotListener { value, error ->
     if (error != null) {
      Log.w(ContentValues.TAG, "Listen failed.", error)
      return@addSnapshotListener
     }

     val games = mutableListOf<GameUser>()
     for (game in value!!) {
      var penal1 = 0
      var penal2 = 0
      if (game.getDouble(Constants.PENAL1) != null)
       penal1 = game.getDouble(Constants.PENAL1)!!.toInt()
      if (game.getDouble(Constants.PENAL2) != null)
       penal2 = game.getDouble(Constants.PENAL2)!!.toInt()
      val newGame = GameUser(
       game.id,
       game.getString(Constants.TEAM1)!!,
       game.getString(Constants.TEAM2)!!,
       game.getString(Constants.FLAG1)!!,
       game.getString(Constants.FLAG2)!!,
       game.getDate(Constants.DATE)!!,
       game.getDouble(Constants.GOALS1)!!.toInt(),
       game.getDouble(Constants.GOALS2)!!.toInt(),
       penal1,
       penal2,
       game.getString(Constants.ROUND)!!,
       game.getDouble(Constants.NUMBER)!!.toInt(),
       game.getDouble(Constants.POINTS)!!.toInt()
      )
      games.add(newGame)
     }
     trySend(games)
    }
   awaitClose { request.remove() }
  }
 }
}