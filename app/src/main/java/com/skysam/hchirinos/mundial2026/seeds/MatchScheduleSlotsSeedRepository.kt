package com.skysam.hchirinos.mundial2026.seeds

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.skysam.hchirinos.mundial2026.BuildConfig
import com.skysam.hchirinos.mundial2026.common.Constants
import com.skysam.hchirinos.mundial2026.common.WorldCup2026MatchScheduleSlots
import com.skysam.hchirinos.mundial2026.common.WorldCup2026MatchScheduleSlots.toEntity
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Siembra los slots oficiales del calendario de eliminatorias en Firestore
 * (colección `match_schedule_slots`).
 *
 * Idempotente:
 *  - Usa id estable `${tournamentId}_slot_${matchNumber}`.
 *  - Usa `set(..., SetOptions.merge())`, así que re-ejecutar:
 *      - no crea documentos duplicados,
 *      - actualiza los campos que cambien en el seed (ej. horario o sede),
 *      - no borra campos que existan en Firestore pero NO en el seed
 *        (ej. `linkedGameId` seteado por otro proceso/Cloud Function).
 *
 * NO se invoca automáticamente. Es responsabilidad de un flujo de admin /
 * desarrollo dispararlo manualmente — ver KDoc de [seedSlots].
 *
 * Nota sobre timestamps:
 *  - `updatedAt` se setea a `Timestamp.now()` en cada ejecución.
 *  - `createdAt` también se setea a `Timestamp.now()` en cada ejecución; con
 *    `merge=true` esto re-stampea el campo en cada re-seed. Aceptable para
 *    una operación de seed administrativa rara. Si más adelante se requiere
 *    `createdAt` real, hacer un read-before-write o delegarlo a una Cloud
 *    Function trigger en `onCreate`.
 */
@Singleton
class MatchScheduleSlotsSeedRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    /**
     * Escribe los 32 slots oficiales de eliminatoria (matches 73..104) para
     * el [tournamentId] indicado.
     *
     * Ejemplo de invocación desde una corrutina (debug menu, REPL, o una
     * línea TEMPORAL en cualquier `viewModelScope.launch { … }` que ya tengas
     * abierta):
     * ```
     * viewModelScope.launch {
     *     matchScheduleSlotsSeedRepository.seedSlots(BuildConfig.TOURNAMENT_ID)
     * }
     * ```
     * Tras correrlo una vez y verificar en la consola Firestore, **comenta o
     * borra la línea** para que no se ejecute en cada arranque.
     *
     * Devuelve cuando el commit del batch se haya confirmado.
     *
     * @param tournamentId Por defecto el del build actual (real o demo).
     */
    suspend fun seedSlots() {
        val tournamentId = BuildConfig.REAL_TOURNAMENT_ID
        val now = Timestamp.now()
        val collection = firestore.collection(Constants.MATCH_SCHEDULE_SLOTS)
        val batch = firestore.batch()

        val slots = WorldCup2026MatchScheduleSlots.eliminationSlots
        for (slot in slots) {
            val entity = slot.toEntity(tournamentId).copy(
                createdAt = now,
                updatedAt = now
            )
            val docId = "${tournamentId}_slot_${slot.matchNumber}"
            batch.set(collection.document(docId), entity, SetOptions.merge())
        }

        batch.commit().await()
        Log.i(TAG, "Seeded ${slots.size} match_schedule_slots for $tournamentId")
    }

    private companion object {
        const val TAG = "MatchScheduleSlotsSeed"
    }
}
