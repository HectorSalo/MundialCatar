package com.skysam.hchirinos.mundialcatar.dataclass

data class GameEntity(
    val tournamentId: String = "",
    val homeTeamId: String = "",
    val awayTeamId: String = "",
    val date: com.google.firebase.Timestamp? = null,
    val stage: String = MatchStage.GROUP.name,   // "GROUP", "ROUND_OF_16", ...
    val group: String? = null,                   // "A", "B"... o null
    val matchNumber: Int = 0,
    val status: String = MatchStatus.SCHEDULED.name,
    // info de sede
    val venueId: String? = null,
    val venueName: String? = null,
    val venueLocation: String? = null,
    // resultado
    val homeGoals: Int? = null,
    val awayGoals: Int? = null,
    val wentToPenalties: Boolean = false,
    val homePenalties: Int? = null,
    val awayPenalties: Int? = null,
    val createdAt: com.google.firebase.Timestamp? = null,
    val updatedAt: com.google.firebase.Timestamp? = null
)
