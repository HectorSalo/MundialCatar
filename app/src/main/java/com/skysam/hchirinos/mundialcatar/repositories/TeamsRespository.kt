package com.skysam.hchirinos.mundialcatar.repositories

import android.content.ContentValues
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Created by Hector Chirinos on 07/05/2022.
 */

object TeamsRespository {
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
     updateOctavos()
     trySend(teams)
    }
   awaitClose { request.remove() }
  }
 }

 private fun updateOctavos() {
  for (group in GROUPS) {
   getInstance().whereEqualTo(Constants.GROUP, group)
    .orderBy(Constants.POINTS, Query.Direction.DESCENDING)
    .limit(2)
    .get()
    .addOnSuccessListener {
     for (document in it) {

     }
     when(group) {

     }
    }
  }
 }
}