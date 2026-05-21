package com.skysam.hchirinos.mundial2026.repositories

import android.content.ContentValues
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skysam.hchirinos.mundial2026.BuildConfig
import com.skysam.hchirinos.mundial2026.common.Constants
import com.skysam.hchirinos.mundial2026.common.DateUtils
import com.skysam.hchirinos.mundial2026.dataclass.MatchScheduleSlot
import com.skysam.hchirinos.mundial2026.dataclass.MatchScheduleSlotEntity
import com.skysam.hchirinos.mundial2026.dataclass.MatchStage
import com.skysam.hchirinos.mundial2026.dataclass.Venue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lectura de la colección `match_schedule_slots`.
 *
 * Solo expone consultas read-only para `BrowseByDateFragment`. La escritura
 * (seed) vive en [com.skysam.hchirinos.mundial2026.seeds.MatchScheduleSlotsSeedRepository].
 */
@Singleton
class MatchScheduleSlotsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private fun collection(): CollectionReference =
        firestore.collection(Constants.MATCH_SCHEDULE_SLOTS)

    /**
     * Slots oficiales para el día calendario que contiene [day], en la zona
     * local del dispositivo. Mismo patrón que `GamesRepository.getGamesByDate`:
     * filtra por `[startOfDay, startOfNextDay)` con snapshot listener.
     */
    fun getSlotsByDate(day: Date): Flow<List<MatchScheduleSlot>> = callbackFlow {
        val start = DateUtils.startOfDay(day)
        val end = DateUtils.startOfNextDay(day)

        val request = collection()
            .whereEqualTo(Constants.TOURNAMENT_ID, BuildConfig.REAL_TOURNAMENT_ID)
            .whereGreaterThanOrEqualTo(Constants.DATE, start)
            .whereLessThan(Constants.DATE, end)
            .orderBy(Constants.DATE, Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.w(ContentValues.TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }

                val slots = snapshot.documents.mapNotNull { doc ->
                    val entity = doc.toObject(MatchScheduleSlotEntity::class.java)
                        ?: return@mapNotNull null
                    entity.toDomainOrNull(doc.id)
                }

                trySend(slots)
            }

        awaitClose { request.remove() }
    }

    private fun MatchScheduleSlotEntity.toDomainOrNull(id: String): MatchScheduleSlot? {
        val timestamp = date ?: run {
            Log.w(TAG, "Slot $id descartado: date == null")
            return null
        }
        val stageEnum = try {
            MatchStage.valueOf(stage)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Slot $id descartado: stage inválido '$stage'")
            return null
        }

        val venue = Venue(
            id = venueId ?: "",
            name = venueName ?: "",
            location = venueLocation ?: ""
        )

        return MatchScheduleSlot(
            id = id,
            tournamentId = tournamentId,
            matchNumber = matchNumber,
            date = timestamp.toDate(),
            stage = stageEnum,
            venue = venue,
            title = title,
            linkedGameId = linkedGameId
        )
    }

    private companion object {
        const val TAG = "SlotsRepository"
    }
}
