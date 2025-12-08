package com.skysam.hchirinos.mundialcatar.dataclass

/**
 * Created by Hector Chirinos in the home office on 7 dic. 2025
 */
data class GameUser(
    val id: String,
    val userId: String,
    val gameId: String,
    val matchNumber: Int,
    val predictedHomeGoals: Int = 0,
    val predictedAwayGoals: Int = 0,
    val points: Int = 0
)

fun GameScore.toResultSign(): ResultSign = when {
    homeGoals > awayGoals -> ResultSign.HOME_WIN
    homeGoals < awayGoals -> ResultSign.AWAY_WIN
    else -> ResultSign.DRAW
}

fun GameUser.toPredictedScore(): GameScore =
    GameScore(
        homeGoals = predictedHomeGoals,
        awayGoals = predictedAwayGoals
    )

fun GameUser.predictedResultSign(): ResultSign =
    toPredictedScore().toResultSign()
