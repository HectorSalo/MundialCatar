package com.skysam.hchirinos.mundialcatar.repositories

import android.content.ContentValues
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Calendar

/**
 * Created by Hector Chirinos on 06/05/2022.
 */

object GamesRepository {
    private val calendar = Calendar.getInstance()
    init {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
    }


    private fun getInstance(): CollectionReference {
        return FirebaseFirestore.getInstance().collection(Constants.GAMES)
    }

    fun getGamesAfter(): Flow<MutableList<Game>> {
        return callbackFlow {
            val request = getInstance()
                .whereGreaterThanOrEqualTo(Constants.DATE, calendar.time)
                .orderBy(Constants.DATE, Query.Direction.ASCENDING)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.w(ContentValues.TAG, "Listen failed.", error)
                        return@addSnapshotListener
                    }

                    val games = mutableListOf<Game>()
                    for (game in value!!) {
                        val newGame = Game(
                            game.id,
                            game.getString(Constants.TEAM1)!!,
                            game.getString(Constants.TEAM2)!!,
                            game.getDate(Constants.DATE)!!,
                            game.getDouble(Constants.GOALS1)!!.toInt(),
                            game.getDouble(Constants.GOALS2)!!.toInt(),
                            game.getString(Constants.ROUND)!!,
                            game.getDouble(Constants.NUMBER)!!.toInt(),
                            game.getBoolean(Constants.START)!!
                        )
                        games.add(newGame)
                    }
                    trySend(games)
                }
            awaitClose { request.remove() }
        }
    }

    fun getGamesBefore(): Flow<MutableList<Game>> {
        return callbackFlow {
            val request = getInstance()
                .whereLessThan(Constants.DATE, calendar.time)
                .orderBy(Constants.DATE, Query.Direction.DESCENDING)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.w(ContentValues.TAG, "Listen failed.", error)
                        return@addSnapshotListener
                    }

                    val games = mutableListOf<Game>()
                    for (game in value!!) {
                        val newGame = Game(
                            game.id,
                            game.getString(Constants.TEAM1)!!,
                            game.getString(Constants.TEAM2)!!,
                            game.getDate(Constants.DATE)!!,
                            game.getDouble(Constants.GOALS1)!!.toInt(),
                            game.getDouble(Constants.GOALS2)!!.toInt(),
                            game.getString(Constants.ROUND)!!,
                            game.getDouble(Constants.NUMBER)!!.toInt(),
                            game.getBoolean(Constants.START)!!
                        )
                        games.add(newGame)
                    }
                    trySend(games)
                }
            awaitClose { request.remove() }
        }
    }

    fun getAllGames(): Flow<MutableList<Game>> {
        return callbackFlow {
            val request = getInstance()
                .orderBy(Constants.DATE, Query.Direction.ASCENDING)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.w(ContentValues.TAG, "Listen failed.", error)
                        return@addSnapshotListener
                    }

                    val games = mutableListOf<Game>()
                    for (game in value!!) {
                        val newGame = Game(
                            game.id,
                            game.getString(Constants.TEAM1)!!,
                            game.getString(Constants.TEAM2)!!,
                            game.getDate(Constants.DATE)!!,
                            game.getDouble(Constants.GOALS1)!!.toInt(),
                            game.getDouble(Constants.GOALS2)!!.toInt(),
                            game.getString(Constants.ROUND)!!,
                            game.getDouble(Constants.NUMBER)!!.toInt(),
                            game.getBoolean(Constants.START)!!
                        )
                        games.add(newGame)
                    }
                    trySend(games)
                }
            awaitClose { request.remove() }
        }
    }

    fun startsGame(game: Game) {
        getInstance()
            .document(game.id)
            .update(Constants.START, true)
    }

    fun setResultGame(game: Game) {
        getInstance()
            .document(game.id)
            .update(Constants.GOALS1, game.goalsTeam1, Constants.GOALS2, game.goalsTeam2)
            .addOnSuccessListener {
                TeamsRespository.updateTeam(game)
                GamesUsersRepository.updatePointsByGame(game)
            }
    }
}