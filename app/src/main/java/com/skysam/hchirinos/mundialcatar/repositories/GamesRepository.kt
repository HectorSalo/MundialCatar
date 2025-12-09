package com.skysam.hchirinos.mundialcatar.repositories

import android.content.ContentValues
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameEntity
import com.skysam.hchirinos.mundialcatar.dataclass.GameScore
import com.skysam.hchirinos.mundialcatar.dataclass.MatchStage
import com.skysam.hchirinos.mundialcatar.dataclass.MatchStatus
import com.skysam.hchirinos.mundialcatar.dataclass.Venue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * Created by Hector Chirinos on 06/05/2022.
 */

class GamesRepository @Inject constructor(private val firestore: FirebaseFirestore) {

    private val calendar = Calendar.getInstance()
    init {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
    }

    private fun collection(): CollectionReference =
        firestore.collection(Constants.GAMES)

    fun getGamesAfter(): Flow<List<Game>> = callbackFlow {
        val request = collection()
            .whereGreaterThanOrEqualTo(Constants.DATE, calendar.time)
            .orderBy(Constants.DATE, Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.w(ContentValues.TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }

                val games = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(GameEntity::class.java)?.toDomain(doc.id)
                }

                trySend(games)
            }

        awaitClose { request.remove() }
    }

    fun getGamesBefore(): Flow<List<Game>> = callbackFlow {
        val request = collection()
            .whereLessThan(Constants.DATE, calendar.time)
            .orderBy(Constants.DATE, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.w(ContentValues.TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }

                val games = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(GameEntity::class.java)?.toDomain(doc.id)
                }

                trySend(games)
            }

        awaitClose { request.remove() }
    }

    fun getAllGames(): Flow<List<Game>> = callbackFlow {
        val request = collection()
            .orderBy(Constants.DATE, Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.w(ContentValues.TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }

                val games = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(GameEntity::class.java)?.toDomain(doc.id)
                }

                trySend(games)
            }

        awaitClose { request.remove() }
    }

    fun GameEntity.toDomain(id: String): Game {
        val stageEnum = MatchStage.valueOf(stage)
        val statusEnum = MatchStatus.valueOf(status)

        val score = if (homeGoals != null && awayGoals != null) {
            GameScore(
                homeGoals = homeGoals,
                awayGoals = awayGoals,
                wentToPenalties = wentToPenalties,
                homePenalties = homePenalties,
                awayPenalties = awayPenalties
            )
        } else {
            null
        }

        val venue = Venue(
            id = venueId ?: "",
            name = venueName ?: "",
            location = venueLocation ?: ""
        )

        return Game(
            id = id,
            tournamentId = tournamentId,
            homeTeamId = homeTeamId,
            awayTeamId = awayTeamId,
            date = date?.toDate() ?: Date(0L),
            stage = stageEnum,
            group = group,
            matchNumber = matchNumber,
            status = statusEnum,
            score = score,
            venue = venue
        )
    }


    fun markGameStarted(gameId: String) {
        collection()
            .document(gameId)
            .update(
                Constants.START, true,
                Constants.STATUS, MatchStatus.SCHEDULED.name,
                Constants.UPDATED_AT, Timestamp.now()
            )
    }

    suspend fun setResultGame(
        gameId: String,
        score: GameScore
    ) {
        val data = hashMapOf<String, Any>(
            Constants.HOME_GOALS to score.homeGoals,
            Constants.AWAY_GOALS to score.awayGoals,
            Constants.WENT_TO_PENALTIES to score.wentToPenalties,
            Constants.HOME_PENALTIES to (score.homePenalties ?: 0),
            Constants.AWAY_PENALTIES to (score.awayPenalties ?: 0),
            Constants.STATUS to MatchStatus.FINISHED.name,
            Constants.UPDATED_AT to Timestamp.now()
        )

        collection()
            .document(gameId)
            .update(data)
            .await()
    }
}