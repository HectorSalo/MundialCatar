package com.skysam.hchirinos.mundialcatar.repositories

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import java.util.*

/**
 * Created by Hector Chirinos on 07/05/2022.
 */

object UpdateRepository {
 private fun getInstance(): CollectionReference {
  return FirebaseFirestore.getInstance().collection(Constants.GAMES)
 }

 fun resultGame(game: Game) {
  val data: Map<String, Any> = hashMapOf(
   Constants.GOALS1 to game.goalsTeam1,
   Constants.GOALS2 to game.goalsTeam2
  )
  getInstance()
   .document(game.id)
   .update(data)
 }

 fun updateTables(teams: MutableList<Team>) {

 }
}