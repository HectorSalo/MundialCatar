package com.skysam.hchirinos.mundialcatar.repositories

import android.content.ContentValues
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.common.Mundial
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameUser
import com.skysam.hchirinos.mundialcatar.dataclass.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.HashMap

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
   Constants.GAMES to user.games,
   Constants.POINTS to user.points
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
      val gamesUser = mutableListOf<GameUser>()
      if (user.get(Constants.GAMES) != null) {
       @Suppress("UNCHECKED_CAST")
       val list = user.data.getValue(Constants.GAMES) as MutableList<HashMap<String, Any>>
       for (item in list) {
        val timestamp: Timestamp = item[Constants.DATE] as Timestamp
        val date = timestamp.toDate()

        val newGame = GameUser(
         item[Constants.TEAM1].toString(),
         item[Constants.TEAM2].toString(),
         date,
         item[Constants.GOALS1].toString().toInt(),
         item[Constants.GOALS2].toString().toInt(),
         round = item[Constants.ROUND].toString(),
         number = item[Constants.NUMBER].toString().toInt(),
         points = item[Constants.POINTS].toString().toInt(),
         predict = item[Constants.PREDICT].toString().toBoolean()
        )
        gamesUser.add(newGame)
       }

       val newUser = User(
        user.id,
        user.getString(Constants.NAME)!!,
        user.getString(Constants.IMAGE)!!,
        user.getString(Constants.EMAIL)!!,
        user.getDouble(Constants.POINTS)!!.toInt(),
        gamesUser
       )
       users.add(newUser)
      }
      trySend(users)
     }
    }
   awaitClose { request.remove() }
  }
 }

 fun getGamesByUser(id: String): Flow<List<GameUser>> {
  return callbackFlow {
   val request = getInstance()
    .document(id)
    .addSnapshotListener { value, error ->
     if (error != null) {
      Log.w(ContentValues.TAG, "Listen failed.", error)
      return@addSnapshotListener
     }

     val gamesUser = mutableListOf<GameUser>()
     if (value?.get(Constants.GAMES) != null) {
      @Suppress("UNCHECKED_CAST")
      val list = value.data?.getValue(Constants.GAMES) as MutableList<HashMap<String, Any>>
      for (item in list) {
       val timestamp: Timestamp = item[Constants.DATE] as Timestamp
       val date = timestamp.toDate()

       val newGame = GameUser(
        item[Constants.TEAM1].toString(),
        item[Constants.TEAM2].toString(),
        date,
        item[Constants.GOALS1].toString().toInt(),
        item[Constants.GOALS2].toString().toInt(),
        round = item[Constants.ROUND].toString(),
        number = item[Constants.NUMBER].toString().toInt(),
        points = item[Constants.POINTS].toString().toInt(),
        predict = item[Constants.PREDICT].toString().toBoolean()
       )
       gamesUser.add(newGame)
      }
     }
     trySend(gamesUser)
    }
   awaitClose { request.remove() }
  }
 }

 fun updatePredict(games: List<GameUser>) {
  getInstance()
   .document(Auth.getCurrenUser()!!.uid)
   .update(Constants.GAMES, games)
 }

 fun updatePointsByGame(game: Game, users: List<User>) {
  for (user in users) {
   getInstance()
    .document(user.id)
    .get()
    .addOnSuccessListener {
     if (it != null) {
      val gamesUser = mutableListOf<GameUser>()
      if (it.get(Constants.GAMES) != null) {
       @Suppress("UNCHECKED_CAST")
       val list = it.data?.getValue(Constants.GAMES) as MutableList<HashMap<String, Any>>
       for (item in list) {
        val timestamp: Timestamp = item[Constants.DATE] as Timestamp
        val date = timestamp.toDate()

        val newGame = GameUser(
         item[Constants.TEAM1].toString(),
         item[Constants.TEAM2].toString(),
         date,
         item[Constants.GOALS1].toString().toInt(),
         item[Constants.GOALS2].toString().toInt(),
         round = item[Constants.ROUND].toString(),
         number = item[Constants.NUMBER].toString().toInt(),
         points = item[Constants.POINTS].toString().toInt(),
         predict = item[Constants.PREDICT].toString().toBoolean()
        )
        gamesUser.add(newGame)
       }
      }

       var gameToUpdate: GameUser? = null
       for (gm in gamesUser) {
        if (gm.number == game.number) {
         gameToUpdate = gm
         break
        }
       }

      var points = 0
      val goals1 = gameToUpdate?.goals1
      val goals2 = gameToUpdate?.goals2

      if (goals1 != null && goals2 != null) {
       if (gameToUpdate!!.predict) {
        if (goals1 == game.goalsTeam1 && goals2 == game.goalsTeam2) {
         points = 5
        }
        if ((goals1 > goals2) && (game.goalsTeam1 > game.goalsTeam2) && (goals1 != game.goalsTeam1 || goals2 != game.goalsTeam2)) {
         points = 3
        }
        if ((goals1 < goals2) && (game.goalsTeam1 < game.goalsTeam2) && (goals1 != game.goalsTeam1 || goals2 != game.goalsTeam2)) {
         points = 3
        }
        if (goals1 == goals2 && game.goalsTeam1 == game.goalsTeam2 && game.goalsTeam1 != goals1)
         points = 3
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

        gameToUpdate.points = points

        gamesUser[gameToUpdate.number - 1] = gameToUpdate

        getInstance()
         .document(user.id)
         .update(Constants.GAMES, gamesUser)
         .addOnSuccessListener { updateAllPoints(gameToUpdate.points.toDouble(), user.id) }
       }
      }
     }
    }
    .addOnFailureListener {
     Log.e("ERROR", it.message.toString())
    }
  }
 }

 private fun updateAllPoints(points: Double, id: String) {
  getInstance()
   .document(id)
   .update(Constants.POINTS, FieldValue.increment(points))
 }
}