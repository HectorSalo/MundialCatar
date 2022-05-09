package com.skysam.hchirinos.mundialcatar.repositories

import android.content.ContentValues
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.*

/**
 * Created by Hector Chirinos on 06/05/2022.
 */

object GamesRepository {
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
                        var gameTo = ""
                        var positionTo = 0
                        var penal1 = 0
                        var penal2 = 0
                        if (game.getString(Constants.GAME_TO) != null)
                            gameTo = game.getString(Constants.GAME_TO)!!
                        if (game.getDouble(Constants.POSITION_TO) != null)
                            positionTo = game.getDouble(Constants.POSITION_TO)!!.toInt()
                        if (game.getDouble(Constants.PENAL1) != null)
                            penal1 = game.getDouble(Constants.PENAL1)!!.toInt()
                        if (game.getDouble(Constants.PENAL2) != null)
                            penal2 = game.getDouble(Constants.PENAL2)!!.toInt()
                        val newGame = Game(
                            game.id,
                            game.getString(Constants.TEAM1)!!,
                            game.getString(Constants.TEAM2)!!,
                            game.getString(Constants.FLAG1)!!,
                            game.getString(Constants.FLAG2)!!,
                            game.getDate(Constants.DATE)!!,
                            game.getString(Constants.STADIUM)!!,
                            game.getDouble(Constants.GOALS1)!!.toInt(),
                            game.getDouble(Constants.GOALS2)!!.toInt(),
                            penal1,
                            penal2,
                            game.getString(Constants.ROUND)!!,
                            game.getDouble(Constants.NUMBER)!!.toInt(),
                            gameTo,
                            positionTo
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
                .orderBy(Constants.DATE, Query.Direction.ASCENDING)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.w(ContentValues.TAG, "Listen failed.", error)
                        return@addSnapshotListener
                    }

                    val games = mutableListOf<Game>()
                    for (game in value!!) {
                        var gameTo = ""
                        var positionTo = 0
                        var penal1 = 0
                        var penal2 = 0
                        if (game.getString(Constants.GAME_TO) != null)
                            gameTo = game.getString(Constants.GAME_TO)!!
                        if (game.getDouble(Constants.POSITION_TO) != null)
                            positionTo = game.getDouble(Constants.POSITION_TO)!!.toInt()
                        if (game.getDouble(Constants.PENAL1) != null)
                            penal1 = game.getDouble(Constants.PENAL1)!!.toInt()
                        if (game.getDouble(Constants.PENAL2) != null)
                            penal2 = game.getDouble(Constants.PENAL2)!!.toInt()
                        val newGame = Game(
                            game.id,
                            game.getString(Constants.TEAM1)!!,
                            game.getString(Constants.TEAM2)!!,
                            game.getString(Constants.FLAG1)!!,
                            game.getString(Constants.FLAG2)!!,
                            game.getDate(Constants.DATE)!!,
                            game.getString(Constants.STADIUM)!!,
                            game.getDouble(Constants.GOALS1)!!.toInt(),
                            game.getDouble(Constants.GOALS1)!!.toInt(),
                            penal1,
                            penal2,
                            game.getString(Constants.ROUND)!!,
                            game.getDouble(Constants.NUMBER)!!.toInt(),
                            gameTo,
                            positionTo
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
                        var gameTo = ""
                        var positionTo = 0
                        var penal1 = 0
                        var penal2 = 0
                        if (game.getString(Constants.GAME_TO) != null)
                            gameTo = game.getString(Constants.GAME_TO)!!
                        if (game.getDouble(Constants.POSITION_TO) != null)
                            positionTo = game.getDouble(Constants.POSITION_TO)!!.toInt()
                        if (game.getDouble(Constants.PENAL1) != null)
                            penal1 = game.getDouble(Constants.PENAL1)!!.toInt()
                        if (game.getDouble(Constants.PENAL2) != null)
                            penal2 = game.getDouble(Constants.PENAL2)!!.toInt()
                        val newGame = Game(
                            game.id,
                            game.getString(Constants.TEAM1)!!,
                            game.getString(Constants.TEAM2)!!,
                            game.getString(Constants.FLAG1)!!,
                            game.getString(Constants.FLAG2)!!,
                            game.getDate(Constants.DATE)!!,
                            game.getString(Constants.STADIUM)!!,
                            game.getDouble(Constants.GOALS1)!!.toInt(),
                            game.getDouble(Constants.GOALS2)!!.toInt(),
                            penal1,
                            penal2,
                            game.getString(Constants.ROUND)!!,
                            game.getDouble(Constants.NUMBER)!!.toInt(),
                            gameTo,
                            positionTo
                        )
                        games.add(newGame)
                    }
                    trySend(games)
                }
            awaitClose { request.remove() }
        }
    }

    fun updateResultGameGroups(game: Game) {
        val data: Map<String, Any> = hashMapOf(
            Constants.GOALS1 to game.goalsTeam1,
            Constants.GOALS2 to game.goalsTeam2
        )
        getInstance()
            .document(game.id)
            .update(data)
            .addOnSuccessListener {
                TeamsRespository.updateTeam(game)
            }
    }

    fun updateResultGamePlayOff(game: Game) {
        val data: Map<String, Any> = hashMapOf(
            Constants.GOALS1 to game.goalsTeam1,
            Constants.GOALS2 to game.goalsTeam2,
            Constants.PENAL1 to game.penal1,
            Constants.PENAL2 to game.penal2
        )
        getInstance()
            .document(game.id)
            .update(data)
            .addOnSuccessListener {
                updatePlyOff(game)
            }
    }

    fun updateOctavos(teams: MutableList<Team>) {
        val team1 = Team("", "", group = Constants.GROUP_B)
        val team2 = Team("", "", group = Constants.GROUP_D)
        val team3 = Team("", "", group = Constants.GROUP_E)
        teams.add(team1)
        teams.add(team2)
        teams.add(team3)
        val list = mutableListOf<Team>()
        for (group in GROUPS) {
            for (team in teams) {
                if (team.group == group) {
                    list.add(team)
                }
            }
        }

        val data49: Map<String, Any> = hashMapOf(
            Constants.TEAM1 to list[0].id,
            Constants.FLAG1 to list[0].flag,
            Constants.TEAM2 to list[5].id,
            Constants.FLAG2 to list[5].flag
        )
        getInstance()
            .document(Constants.GAME_49)
            .update(data49)

        val data51: Map<String, Any> = hashMapOf(
            Constants.TEAM1 to list[4].id,
            Constants.FLAG1 to list[4].flag,
            Constants.TEAM2 to list[1].id,
            Constants.FLAG2 to list[1].flag
        )
        getInstance()
            .document(Constants.GAME_51)
            .update(data51)

        val data50: Map<String, Any> = hashMapOf(
            Constants.TEAM1 to list[8].id,
            Constants.FLAG1 to list[8].flag,
            Constants.TEAM2 to list[13].id,
            Constants.FLAG2 to list[13].flag
        )
        getInstance()
            .document(Constants.GAME_50)
            .update(data50)

        val data52: Map<String, Any> = hashMapOf(
            Constants.TEAM1 to list[12].id,
            Constants.FLAG1 to list[12].flag,
            Constants.TEAM2 to list[9].id,
            Constants.FLAG2 to list[9].flag
        )
        getInstance()
            .document(Constants.GAME_52)
            .update(data52)

        val data53: Map<String, Any> = hashMapOf(
            Constants.TEAM1 to list[16].id,
            Constants.FLAG1 to list[16].flag,
            Constants.TEAM2 to list[21].id,
            Constants.FLAG2 to list[21].flag
        )
        getInstance()
            .document(Constants.GAME_53)
            .update(data53)

        val data54: Map<String, Any> = hashMapOf(
            Constants.TEAM1 to list[24].id,
            Constants.FLAG1 to list[24].flag,
            Constants.TEAM2 to list[29].id,
            Constants.FLAG2 to list[29].flag
        )
        getInstance()
            .document(Constants.GAME_54)
            .update(data54)

        val data55: Map<String, Any> = hashMapOf(
            Constants.TEAM1 to list[20].id,
            Constants.FLAG1 to list[20].flag,
            Constants.TEAM2 to list[17].id,
            Constants.FLAG2 to list[17].flag
        )
        getInstance()
            .document(Constants.GAME_55)
            .update(data55)

        val data56: Map<String, Any> = hashMapOf(
            Constants.TEAM1 to list[28].id,
            Constants.FLAG1 to list[28].flag,
            Constants.TEAM2 to list[25].id,
            Constants.FLAG2 to list[25].flag
        )
        getInstance()
            .document(Constants.GAME_56)
            .update(data56)
    }

    private fun updatePlyOff(game: Game) {
        val team = if (game.positionTo == 1) Constants.TEAM1 else Constants.TEAM2
        val flag = if (game.positionTo == 1) Constants.FLAG1 else Constants.FLAG2
        var data: Map<String, Any>? = null

        if (game.goalsTeam1 > game.goalsTeam2) {
            data = hashMapOf(
                team to game.team1,
                flag to game.team1
            )
        }

        if (game.goalsTeam1 < game.goalsTeam2) {
            data = hashMapOf(
                team to game.team2,
                flag to game.team2
            )
        }

        if (game.goalsTeam1 == game.goalsTeam2) {
            if (game.penal1 > game.penal2) {
                data = hashMapOf(
                    team to game.team1,
                    flag to game.team1
                )
            }
            if (game.penal1 < game.penal2) {
                data = hashMapOf(
                    team to game.team2,
                    flag to game.team2
                )
            }
        }

        getInstance()
            .document(game.gameTo)
            .update(data!!)
    }
}