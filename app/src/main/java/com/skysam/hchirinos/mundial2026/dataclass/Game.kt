package com.skysam.hchirinos.mundial2026.dataclass

import java.util.*

/**
 * Created by Hector Chirinos on 03/05/2022.
 */

enum class MatchStage {
    GROUP,
    ROUND_OF_32,
    ROUND_OF_16,
    QUARTER_FINAL,
    SEMI_FINAL,
    THIRD_PLACE,
    FINAL
}

enum class MatchStatus {
    SCHEDULED,
    FINISHED
}

data class GameScore(
    val homeGoals: Int,
    val awayGoals: Int,
    val wentToPenalties: Boolean = false,
    val homePenalties: Int? = null,
    val awayPenalties: Int? = null
)

data class Game(
    val id: String,
    val tournamentId: String,
    val homeTeamId: String,
    val awayTeamId: String,
    val date: Date,
    val stage: MatchStage,
    val group: String?,        // A, B, C… o null en eliminatorias
    val matchNumber: Int,
    val status: MatchStatus,
    val score: GameScore?, // null si aún n
    val venue: Venue,
    val canPredicted: Boolean?
)

data class Venue(
    val id: String = "",
    val name: String = "",
    val location: String = ""
)





