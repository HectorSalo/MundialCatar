package com.skysam.hchirinos.mundialcatar.dataclass

import androidx.annotation.DrawableRes

/**
 * Created by Hector Chirinos in the home office on 8 dic. 2025
 */
data class GroupStandingUi(
    val teamId: String,
    val teamName: String,
    @DrawableRes val flagUrl: Int,
    val group: String,
    val played: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDiff: Int,
    val points: Int,
    val position: Int,                  // 1, 2, 3, ...
    val qualifiesAsTopTwo: Boolean,     // top 2 del grupo
    val qualifiesAsBestThird: Boolean   // uno de los 8 mejores terceros
)

data class MutableTeamStats(
    val teamId: String,
    val teamName: String,
    @DrawableRes val flagUrl: Int,
    val group: String,
    var played: Int = 0,
    var wins: Int = 0,
    var draws: Int = 0,
    var losses: Int = 0,
    var goalsFor: Int = 0,
    var goalsAgainst: Int = 0,
    var points: Int = 0
)
