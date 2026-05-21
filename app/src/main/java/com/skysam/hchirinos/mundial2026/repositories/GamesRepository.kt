package com.skysam.hchirinos.mundial2026.repositories

import android.content.ContentValues
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skysam.hchirinos.mundial2026.BuildConfig
import com.skysam.hchirinos.mundial2026.common.Constants
import com.skysam.hchirinos.mundial2026.common.DateUtils
import com.skysam.hchirinos.mundial2026.dataclass.Game
import com.skysam.hchirinos.mundial2026.dataclass.GameEntity
import com.skysam.hchirinos.mundial2026.dataclass.GameScore
import com.skysam.hchirinos.mundial2026.dataclass.MatchStage
import com.skysam.hchirinos.mundial2026.dataclass.MatchStatus
import com.skysam.hchirinos.mundial2026.dataclass.Venue
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
            .whereEqualTo(Constants.TOURNAMENT_ID, BuildConfig.TOURNAMENT_ID)
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
            .whereEqualTo(Constants.TOURNAMENT_ID, BuildConfig.TOURNAMENT_ID)
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

    fun getGamesByDate(day: Date): Flow<List<Game>> = callbackFlow {
        val start = DateUtils.startOfDay(day)
        val end = DateUtils.startOfNextDay(day)

        val request = collection()
            .whereEqualTo(Constants.TOURNAMENT_ID, BuildConfig.TOURNAMENT_ID)
            .whereGreaterThanOrEqualTo(Constants.DATE, start)
            .whereLessThan(Constants.DATE, end)
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

    fun getAllGames(): Flow<List<Game>> = callbackFlow {
        val request = collection()
            .whereEqualTo(Constants.TOURNAMENT_ID, BuildConfig.TOURNAMENT_ID)
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
            venue = venue,
            canPredicted = canPredicted
        )
    }

    suspend fun setResultGame(
        gameId: String,
        score: GameScore
    ) {
        val data = hashMapOf<String, Any?>(
            Constants.HOME_GOALS to score.homeGoals,
            Constants.AWAY_GOALS to score.awayGoals,
            Constants.WENT_TO_PENALTIES to score.wentToPenalties,
            Constants.HOME_PENALTIES to (score.homePenalties),
            Constants.AWAY_PENALTIES to (score.awayPenalties),
            Constants.STATUS to MatchStatus.FINISHED.name,
            Constants.UPDATED_AT to Timestamp.now()
        )

        collection()
            .document(gameId)
            .update(data)
            .await()
    }
}