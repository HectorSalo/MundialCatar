package com.skysam.hchirinos.mundialcatar.dataclass

import java.util.Date

data class GameToView(
    val homeTeamName: String,
    val awayTeamName: String,
    val flag1: String,
    val flag2: String,
    val date: Date,
    var homeGoals: Int = 0,
    var awayGoals: Int = 0,
    val round: String,
    val number: Int,
    var points: Int = 0,
    val hasPrediction: Boolean = false,
    val gameId: String = "",
    val tournamentId: String = ""
)
