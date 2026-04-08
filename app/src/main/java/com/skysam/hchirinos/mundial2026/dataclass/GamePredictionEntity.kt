package com.skysam.hchirinos.mundial2026.dataclass

import java.util.Date

/**
 * Created by Hector Chirinos on 11/05/2022.
 */

data class GamePredictionEntity(
    val id: String,                 // id del doc en Firestore (p.ej. "userId_matchNumber")
    val userId: String,
    val gameId: String,             // id del Game en Firestore
    val tournamentId: String,
    val matchNumber: Int,           // mismo que Game.matchNumber
    val predictedHomeGoals: Int,
    val predictedAwayGoals: Int,
    val points: Int,                // puntos calculados para esta predicción
    val createdAt: Date?,
    val updatedAt: Date?,
    val hasPendingWrites: Boolean = false,
    val isFromCache: Boolean = false
)
