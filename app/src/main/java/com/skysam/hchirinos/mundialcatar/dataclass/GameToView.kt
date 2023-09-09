package com.skysam.hchirinos.mundialcatar.dataclass

import java.util.Date

data class GameToView(
    val team1: String,
    val team2: String,
    val flag1: String,
    val flag2: String,
    val date: Date,
    var goalsTeam1: Int = 0,
    var goalsTeam2: Int = 0,
    val round: String,
    val number: Int
)
