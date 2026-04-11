package com.skysam.hchirinos.mundial2026.seeds

data class DemoPredictionSeed(
    val matchNumber: Int,
    val predictedHomeGoals: Int,
    val predictedAwayGoals: Int
)

data class DemoGameResultSeed(
    val matchNumber: Int,
    val homeGoals: Int,
    val awayGoals: Int,
    val wentToPenalties: Boolean = false,
    val homePenalties: Int? = null,
    val awayPenalties: Int? = null
)
