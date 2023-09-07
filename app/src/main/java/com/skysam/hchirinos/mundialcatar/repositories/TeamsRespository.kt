package com.skysam.hchirinos.mundialcatar.repositories

import android.content.ContentValues
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Created by Hector Chirinos on 07/05/2022.
 */

object TeamsRespository {
 private fun getInstance(): CollectionReference {
  return FirebaseFirestore.getInstance().collection(Constants.TEAMS)
 }

 fun getAllTeams(): Flow<MutableList<Team>> {
  return callbackFlow {
   val request = getInstance()
    .orderBy(Constants.POINTS, Query.Direction.DESCENDING)
    .addSnapshotListener { value, error ->
     if (error != null) {
      Log.w(ContentValues.TAG, "Listen failed.", error)
      return@addSnapshotListener
     }

     val teams = mutableListOf<Team>()
     for (team in value!!) {
      val newTeam = Team(
       team.id,
       team.getString(Constants.FLAG)!!,
       team.getDouble(Constants.POINTS)!!.toInt(),
       team.getDouble(Constants.GOALS_MADE)!!.toInt(),
       team.getDouble(Constants.GOALS_CONCEDED)!!.toInt(),
       team.getDouble(Constants.WINS)!!.toInt(),
       team.getDouble(Constants.DEFEATS)!!.toInt(),
       team.getDouble(Constants.TIED)!!.toInt(),
       team.getString(Constants.GROUP)!!
      )
      teams.add(newTeam)
     }
     trySend(teams)
    }
   awaitClose { request.remove() }
  }
 }

 fun getTeamById(id: String): Flow<Team> {
  return callbackFlow {
   val request = getInstance()
    .document(id)
    .get()
    .addOnSuccessListener{ team ->
     val newTeam = Team(
      team.id,
      team.getString(Constants.FLAG)!!,
      team.getDouble(Constants.POINTS)!!.toInt(),
      team.getDouble(Constants.GOALS_MADE)!!.toInt(),
      team.getDouble(Constants.GOALS_CONCEDED)!!.toInt(),
      team.getDouble(Constants.WINS)!!.toInt(),
      team.getDouble(Constants.DEFEATS)!!.toInt(),
      team.getDouble(Constants.TIED)!!.toInt(),
      team.getString(Constants.GROUP)!!
     )
     trySend(newTeam)
    }
    .addOnFailureListener { Log.w(ContentValues.TAG, "Listen failed.", it) }
   awaitClose {  }
  }
 }



 fun updateTeam(game: Game) {
  if (game.goalsTeam1 > game.goalsTeam2) {
   val data1: Map<String, Any> = hashMapOf(
    Constants.GOALS_MADE to FieldValue.increment(game.goalsTeam1.toDouble()),
    Constants.GOALS_CONCEDED to FieldValue.increment(game.goalsTeam2.toDouble()),
    Constants.WINS to FieldValue.increment(1),
    Constants.POINTS to FieldValue.increment(3)
   )

   val data2: Map<String, Any> = hashMapOf(
    Constants.GOALS_MADE to FieldValue.increment(game.goalsTeam2.toDouble()),
    Constants.GOALS_CONCEDED to FieldValue.increment(game.goalsTeam1.toDouble()),
    Constants.DEFEATS to FieldValue.increment(1)
   )

   getInstance()
    .document(game.team1)
    .update(data1)
   getInstance()
    .document(game.team2)
    .update(data2)
  }

  if (game.goalsTeam1 < game.goalsTeam2) {
   val data1: Map<String, Any> = hashMapOf(
    Constants.GOALS_MADE to FieldValue.increment(game.goalsTeam1.toDouble()),
    Constants.GOALS_CONCEDED to FieldValue.increment(game.goalsTeam2.toDouble()),
    Constants.DEFEATS to FieldValue.increment(1)
   )

   val data2: Map<String, Any> = hashMapOf(
    Constants.GOALS_MADE to FieldValue.increment(game.goalsTeam2.toDouble()),
    Constants.GOALS_CONCEDED to FieldValue.increment(game.goalsTeam1.toDouble()),
    Constants.WINS to FieldValue.increment(1),
    Constants.POINTS to FieldValue.increment(3)
   )

   getInstance()
    .document(game.team1)
    .update(data1)
   getInstance()
    .document(game.team2)
    .update(data2)
  }

  if (game.goalsTeam1 == game.goalsTeam2) {
   val data1: Map<String, Any> = hashMapOf(
    Constants.GOALS_MADE to FieldValue.increment(game.goalsTeam1.toDouble()),
    Constants.GOALS_CONCEDED to FieldValue.increment(game.goalsTeam2.toDouble()),
    Constants.TIED to FieldValue.increment(1),
    Constants.POINTS to FieldValue.increment(1)
   )

   val data2: Map<String, Any> = hashMapOf(
    Constants.GOALS_MADE to FieldValue.increment(game.goalsTeam2.toDouble()),
    Constants.GOALS_CONCEDED to FieldValue.increment(game.goalsTeam1.toDouble()),
    Constants.TIED to FieldValue.increment(1),
    Constants.POINTS to FieldValue.increment(1)
   )

   getInstance()
    .document(game.team1)
    .update(data1)
   getInstance()
    .document(game.team2)
    .update(data2)
  }
 }
}