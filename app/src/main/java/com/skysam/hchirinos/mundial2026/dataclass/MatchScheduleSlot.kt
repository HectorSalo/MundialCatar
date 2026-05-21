package com.skysam.hchirinos.mundial2026.dataclass

import java.util.Date

/**
 * "Slot oficial" del calendario del torneo: una fecha/hora/sede donde habrá un
 * partido oficial de FIFA, aunque los equipos todavía no estén definidos.
 *
 * Solo se usa para fases de eliminatoria (matches 73..104 del Mundial 2026).
 * NO se sirve como partido jugable: no tiene marcador, no se pronostica y no
 * participa en cálculos de grupos. Su único propósito es que el calendario
 * sea completo cuando todavía no existe el [Game] real.
 *
 * Cuando el [Game] real ya existe en la colección `games`, el slot puede
 * apuntar a él vía [linkedGameId] para que el cliente evite mostrar la fila
 * duplicada.
 */
data class MatchScheduleSlot(
    val id: String,
    val tournamentId: String,
    val matchNumber: Int,
    val date: Date,
    val stage: MatchStage,
    val venue: Venue,
    /**
     * Texto corto que la UI muestra como título del partido,
     * ej. "Final del Mundial", "Octavos de Final", "Cuartos de Final".
     */
    val title: String,
    val linkedGameId: String?
)
