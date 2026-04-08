package com.skysam.hchirinos.mundial2026.common

import com.google.firebase.Timestamp
import com.skysam.hchirinos.mundial2026.dataclass.GameEntity
import com.skysam.hchirinos.mundial2026.dataclass.MatchStage
import com.skysam.hchirinos.mundial2026.dataclass.MatchStatus

import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Created by Hector Chirinos in the home office on 8 dic. 2025
 */
object WorldCup2026Games {
    data class GameSeed(
        val matchNumber: Int,
        val stage: MatchStage,
        val group: String?,              // "A".."L" en fase de grupos, null en eliminatoria
        val homeTeamCode: String,        // Debe coincidir con TeamSeed.code (MEX, RSA, PL_A, etc.)
        val awayTeamCode: String,
        val localDateTime: LocalDateTime, // fecha y hora LOCAL del estadio
        val venueId: String? ,      // id interno (ej. "TORONTO_BMO_FIELD")
        val venueName: String?,    // nombre del estadio (ej. "BMO Field")
        val venueLocation: String? // ubicación (ej. "Toronto, Canadá")
    )

    // En WorldCup2026Games

    val groupStageGames: List<WorldCup2026Games.GameSeed> = listOf(
        GameSeed(
            matchNumber = 21,
            stage = MatchStage.GROUP,
            group = "L",
            homeTeamCode = "GHA",                    // Ghana
            awayTeamCode = "PAN",                    // Panamá
            localDateTime = LocalDateTime.of(2026, 6, 17, 19, 0),
            venueId = "TORONTO_BMO_FIELD",
            venueName = "BMO Field",
            venueLocation = "Toronto, Canada"
        ),
        GameSeed(
            matchNumber = 22,
            stage = MatchStage.GROUP,
            group = "L",
            homeTeamCode = "ENG",                    // Inglaterra
            awayTeamCode = "CRO",                    // Croacia
            localDateTime = LocalDateTime.of(2026, 6, 17, 15, 0),
            venueId = "ARLINGTON_ATT_STADIUM",
            venueName = "AT&T Stadium",
            venueLocation = "Arlington, United States"
        ),

        // Jornada 2
        GameSeed(
            matchNumber = 45,
            stage = MatchStage.GROUP,
            group = "L",
            homeTeamCode = "ENG",                    // Inglaterra
            awayTeamCode = "GHA",                    // Ghana
            localDateTime = LocalDateTime.of(2026, 6, 23, 16, 0),
            venueId = "FOXBOROUGH_GILLETTE_STADIUM",
            venueName = "Gillette Stadium",
            venueLocation = "Foxborough, United States"
        ),
        GameSeed(
            matchNumber = 46,
            stage = MatchStage.GROUP,
            group = "L",
            homeTeamCode = "PAN",                    // Panamá
            awayTeamCode = "CRO",                    // Croacia
            localDateTime = LocalDateTime.of(2026, 6, 23, 19, 0),
            venueId = "TORONTO_BMO_FIELD",
            venueName = "BMO Field",
            venueLocation = "Toronto, Canada"
        ),

        // Jornada 3
        GameSeed(
            matchNumber = 67,
            stage = MatchStage.GROUP,
            group = "L",
            homeTeamCode = "PAN",                    // Panamá
            awayTeamCode = "ENG",                    // Inglaterra
            localDateTime = LocalDateTime.of(2026, 6, 27, 17, 0),
            venueId = "EAST_RUTHERFORD_METLIFE_STADIUM",
            venueName = "MetLife Stadium",
            venueLocation = "East Rutherford, United States"
        ),
        GameSeed(
            matchNumber = 68,
            stage = MatchStage.GROUP,
            group = "L",
            homeTeamCode = "CRO",                    // Croacia
            awayTeamCode = "GHA",                    // Ghana
            localDateTime = LocalDateTime.of(2026, 6, 27, 17, 0),
            venueId = "PHILADELPHIA_LINCOLN_FINANCIAL_FIELD",
            venueName = "Lincoln Financial Field",
            venueLocation = "Philadelphia, United States"
        )
    )

    fun GameSeed.toEntity(
        tournamentId: String = WorldCup2026Teams.TOURNAMENT_ID,
        zoneId: ZoneId = ZoneId.of("UTC") // o la zona que quieras usar como referencia
    ): GameEntity {
        val instant = localDateTime.atZone(zoneId).toInstant()
        val timestamp = Timestamp(instant.epochSecond, instant.nano)

        return GameEntity(
            tournamentId = tournamentId,
            homeTeamId = homeTeamCode,          // en Firestore guardarás el code (MEX, RSA, etc.)
            awayTeamId = awayTeamCode,
            date = timestamp,
            stage = stage.name,
            group = group,
            matchNumber = matchNumber,
            status = MatchStatus.SCHEDULED.name,

            venueId = venueId,
            venueName = venueName,
            venueLocation = venueLocation,

            homeGoals = null,
            awayGoals = null,
            wentToPenalties = false,
            homePenalties = null,
            awayPenalties = null,
            createdAt = null,
            updatedAt = null
        )
    }
}