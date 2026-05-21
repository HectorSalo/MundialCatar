package com.skysam.hchirinos.mundial2026.dataclass

import com.google.firebase.Timestamp

/**
 * Entidad Firestore para la colección `match_schedule_slots`.
 *
 * Mismo patrón que [GameEntity]: campos planos, defaults para que el
 * constructor sin argumentos requerido por Firestore funcione, enum
 * serializado como String y [Venue] aplanado en tres campos. La conversión
 * a dominio ([MatchScheduleSlot]) se hace en el repositorio.
 */
data class MatchScheduleSlotEntity(
    val tournamentId: String = "",
    val matchNumber: Int = 0,
    val date: Timestamp? = null,
    val stage: String = MatchStage.ROUND_OF_32.name,
    val venueId: String? = null,
    val venueName: String? = null,
    val venueLocation: String? = null,
    val title: String = "",
    val linkedGameId: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
