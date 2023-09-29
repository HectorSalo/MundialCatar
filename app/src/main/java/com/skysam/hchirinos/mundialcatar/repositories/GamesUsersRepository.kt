package com.skysam.hchirinos.mundialcatar.repositories

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.common.Mundial
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Created by Hector Chirinos on 28/09/2023.
 */

object GamesUsersRepository {
    private fun getInstance(): CollectionReference {
        return FirebaseFirestore.getInstance().collection(Mundial.Mundial.getContext().getString(R.string.path_games_users))
    }

    fun createPredict(gameUser: GameUser) {
        val data = hashMapOf(
            Constants.GOALS1 to gameUser.goals1,
            Constants.GOALS2 to gameUser.goals2,
            Constants.NUMBER to gameUser.number,
            Constants.POINTS to gameUser.points,
            Constants.ID_USER to Auth.getCurrenUser()?.uid

        )
        getInstance()
            .add(data)
    }

    fun updatePredict(gameUser: GameUser) {
        val data: HashMap<String, Any?> = hashMapOf(
            Constants.GOALS1 to gameUser.goals1,
            Constants.GOALS2 to gameUser.goals2,
            Constants.NUMBER to gameUser.number,
            Constants.POINTS to gameUser.points,
            Constants.ID_USER to Auth.getCurrenUser()?.uid

        )
        getInstance()
            .document(gameUser.id)
            .update(data)
    }

    fun getGamesByUser(): Flow<List<GameUser>> {
        return callbackFlow {
            val request = getInstance()
                .whereEqualTo(Constants.ID_USER, Auth.getCurrenUser()!!.uid)
                .orderBy(Constants.NUMBER, Query.Direction.ASCENDING)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.w(ContentValues.TAG, "Listen failed.", error)
                        return@addSnapshotListener
                    }

                    val gamesUser = mutableListOf<GameUser>()
                    for (gm in value!!) {
                        val newGame = GameUser(
                            gm.id,
                            gm.getString(Constants.ID_USER)!!,
                            gm.getDouble(Constants.GOALS1)!!.toInt(),
                            gm.getDouble(Constants.GOALS2)!!.toInt(),
                            gm.getDouble(Constants.NUMBER)!!.toInt(),
                            gm.getDouble(Constants.POINTS)!!.toInt()
                        )
                        gamesUser.add(newGame)
                    }
                    trySend(gamesUser)
                }
            awaitClose { request.remove() }
        }
    }

    fun updatePointsByGame(game: Game) {
        getInstance()
            .whereEqualTo(Constants.NUMBER, game.number)
            .get()
            .addOnSuccessListener { value ->
                val gamesUser = mutableListOf<GameUser>()
                for (gm in value!!) {
                    val newGame = GameUser(
                        gm.id,
                        gm.getString(Constants.ID_USER)!!,
                        gm.getDouble(Constants.GOALS1)!!.toInt(),
                        gm.getDouble(Constants.GOALS2)!!.toInt(),
                        gm.getDouble(Constants.NUMBER)!!.toInt(),
                        gm.getDouble(Constants.POINTS)!!.toInt()
                    )
                    gamesUser.add(newGame)
                }

                for (gm in gamesUser) {
                    var points = 0
                    val goals1 = gm.goals1
                    val goals2 = gm.goals2

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

                    getInstance()
                        .document(gm.id)
                        .update(Constants.POINTS, points)
                        .addOnSuccessListener {
                            UsersRepository.updateAllPoints(
                                points.toDouble(),
                                gm.idUser
                            )
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }
}