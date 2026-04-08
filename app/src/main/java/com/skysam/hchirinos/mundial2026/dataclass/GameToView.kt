package com.skysam.hchirinos.mundial2026.dataclass

import androidx.annotation.DrawableRes
import java.util.Date

data class GameToView(
    val homeTeamName: String,
    val awayTeamName: String,
    @DrawableRes val flag1Res: Int,
    @DrawableRes val flag2Res: Int,
    val date: Date,
    var homeGoals: Int? = 0,
    var awayGoals: Int? = 0,
    val round: String,
    val number: Int,
    var points: Int = 0,
    val hasPrediction: Boolean = false,
    val gameId: String = "",
    val tournamentId: String = "",
    val stadiumName: String,
    val stadiumCity: String
)
