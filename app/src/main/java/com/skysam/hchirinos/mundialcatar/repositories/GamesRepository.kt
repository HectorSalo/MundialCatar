package com.skysam.hchirinos.mundialcatar.repositories

import android.content.ContentValues
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.common.WorldCup2026Games
import com.skysam.hchirinos.mundialcatar.common.WorldCup2026Games.toEntity
import com.skysam.hchirinos.mundialcatar.common.WorldCup2026Teams
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameEntity
import com.skysam.hchirinos.mundialcatar.dataclass.GameScore
import com.skysam.hchirinos.mundialcatar.dataclass.MatchStage
import com.skysam.hchirinos.mundialcatar.dataclass.MatchStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * Created by Hector Chirinos on 06/05/2022.
 */

class GamesRepository @Inject constructor(private val firestore: FirebaseFirestore) {

    private fun lt(h: Int, m: Int) = LocalTime.of(h, m)
    private val calendar = Calendar.getInstance()
    init {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
    }

    private val ZONE_VENEZUELA: ZoneId = ZoneId.of("America/Caracas")

    val OFFICIAL_GROUP_STAGE_TIMES_VE: Map<Int, LocalTime> = mapOf(
        1  to lt(15, 0),
        2  to lt(22, 0),
        3  to lt(15, 0),
        4  to lt(21, 0),
        5  to lt(21, 0),
        6  to lt(0,  0),
        7  to lt(18, 0),
        8  to lt(15, 0),
        9  to lt(19, 0),
        10 to lt(13, 0),
        11 to lt(16, 0),
        12 to lt(22, 0),
        13 to lt(18, 0),
        14 to lt(12, 0),
        15 to lt(21, 0),
        16 to lt(15, 0),
        17 to lt(15, 0),
        18 to lt(18, 0),
        19 to lt(21, 0),
        20 to lt(0,  0),
        21 to lt(19, 0),
        22 to lt(16, 0),
        23 to lt(13, 0),
        24 to lt(22, 0),
        25 to lt(12, 0),
        26 to lt(15, 0),
        27 to lt(18, 0),
        28 to lt(21, 0),
        29 to lt(21, 0),
        30 to lt(18, 0),
        31 to lt(0,  0),
        32 to lt(15, 0),
        33 to lt(16, 0),
        34 to lt(20, 0),
        35 to lt(13, 0),
        36 to lt(0,  0),
        37 to lt(18, 0),
        38 to lt(12, 0),
        39 to lt(15, 0),
        40 to lt(21, 0),
        41 to lt(20, 0),
        42 to lt(17, 0),
        43 to lt(13, 0),
        44 to lt(23, 0),
        45 to lt(16, 0),
        46 to lt(19, 0),
        47 to lt(13, 0),
        48 to lt(22, 0),
        49 to lt(18, 0),
        50 to lt(18, 0),
        51 to lt(15, 0),
        52 to lt(15, 0),
        53 to lt(21, 0),
        54 to lt(21, 0),
        55 to lt(16, 0),
        56 to lt(16, 0),
        57 to lt(19, 0),
        58 to lt(19, 0),
        59 to lt(22, 0),
        60 to lt(22, 0),
        61 to lt(15, 0),
        62 to lt(15, 0),
        63 to lt(23, 0),
        64 to lt(23, 0),
        65 to lt(20, 0),
        66 to lt(20, 0),
        67 to lt(17, 0),
        68 to lt(17, 0),
        69 to lt(22, 0),
        70 to lt(22, 0),
        71 to lt(19, 30),
        72 to lt(19, 30)
    )


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
            score = score
        )
    }

    fun Game.toEntity(now: Timestamp = Timestamp.now()): GameEntity =
        GameEntity(
            tournamentId = tournamentId,
            homeTeamId = homeTeamId,
            awayTeamId = awayTeamId,
            date = Timestamp(date),
            stage = stage.name,
            group = group,
            matchNumber = matchNumber,
            status = status.name,
            homeGoals = score?.homeGoals,
            awayGoals = score?.awayGoals,
            wentToPenalties = score?.wentToPenalties ?: false,
            homePenalties = score?.homePenalties,
            awayPenalties = score?.awayPenalties,
            createdAt = createdAtOrNull(),   // si quieres manejarlo tú, o lo quitas
            updatedAt = now
        )

    // Si no quieres manejar createdAt/updatedAt aquí, quita esas dos líneas y campos.
    private fun createdAtOrNull(): Timestamp? = null


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

    suspend fun logGroupStageTimesByHour(
        worldCup2026TournamentId: String
    ) {
        val snapshot = collection()
            .whereEqualTo(Constants.TOURNAMENT_ID, worldCup2026TournamentId)
            .whereEqualTo(Constants.STAGE, MatchStage.GROUP.name)
            .get()
            .await()

        if (snapshot.isEmpty) {
            Log.i("GamesTimeCheck", "No hay partidos de fase de grupos para ese torneo.")
            return
        }

        // (hora -> lista de matchNumbers)
        val grouped = snapshot.documents.mapNotNull { doc ->
            val ts = doc.getTimestamp(Constants.DATE) ?: return@mapNotNull null
            val ldt = ts.toVenezuelaLocalDateTime()
            val time = ldt.toLocalTime().withSecond(0).withNano(0)

            val matchNumber = doc.getLong(Constants.MATCH_NUMBER)?.toInt()
                ?: return@mapNotNull null

            time to matchNumber
        }.groupBy({ it.first }, { it.second })

        // Log ordenado por hora
        grouped.toSortedMap().forEach { (time, matches) ->
            Log.i(
                "GamesTimeCheck",
                "Hora actual $time -> matchNumbers: ${matches.sorted()}"
            )
        }
    }

    // Convierte Timestamp de Firestore a LocalDateTime en Venezuela
    fun Timestamp.toVenezuelaLocalDateTime(): LocalDateTime =
        this.toDate().toInstant().atZone(ZONE_VENEZUELA).toLocalDateTime()

    // Convierte LocalDateTime (Venezuela) a Timestamp para Firestore
    fun LocalDateTime.toVenezuelaTimestamp(): Timestamp {
        val instant = this.atZone(ZONE_VENEZUELA).toInstant()
        return Timestamp(Date.from(instant))
    }

    suspend fun patchGroupStageTimesToOfficialVe(
        worldCup2026TournamentId: String
    ) {
        val snapshot = collection()
            .whereEqualTo(Constants.TOURNAMENT_ID, worldCup2026TournamentId)
            .whereEqualTo(Constants.STAGE, MatchStage.GROUP.name)
            .get()
            .await()

        if (snapshot.isEmpty) return

        firestore.runBatch { batch ->
            snapshot.documents.forEach { doc ->
                val ts = doc.getTimestamp(Constants.DATE) ?: return@forEach
                val oldLdt = ts.toVenezuelaLocalDateTime()

                val matchNumber = doc.getLong(Constants.MATCH_NUMBER)?.toInt()
                    ?: return@forEach

                val newTime = OFFICIAL_GROUP_STAGE_TIMES_VE[matchNumber]
                    ?: return@forEach

                val newLdt = oldLdt
                    .withHour(newTime.hour)
                    .withMinute(newTime.minute)
                    .withSecond(0)
                    .withNano(0)

                val newTs = newLdt.toVenezuelaTimestamp()

                batch.update(
                    doc.reference,
                    Constants.DATE, newTs,
                    Constants.UPDATED_AT, Timestamp.now()
                )
            }
        }.await()
    }



    /*suspend fun seedWorldCup2026IfNeeded() {
        // 1) Buscar todos los juegos del torneo 2026
        val snapshot = collection()
            .whereEqualTo(Constants.GROUP, "C")
            .get()
            .await()

        // 2) Borrar esos juegos y volver a insertar los seeds en un solo batch
        firestore.runBatch { batch ->
            // Borrar existentes de ese torneo
            *//*for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }*//*

            // Insertar todos los partidos de fase de grupos
            WorldCup2026Games.groupStageGames.forEach { seed ->
                // Puedes usar el matchNumber como id de documento
                val docRef = collection().document("match_${seed.matchNumber}")
                batch.set(docRef, seed.toEntity())
            }
        }.await()
    }*/
}