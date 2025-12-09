package com.skysam.hchirinos.mundialcatar.dataclass

/**
 * Created by Hector Chirinos in the home office on 7 dic. 2025
 */
data class GamePrediction(
    val id: String,
    val userId: String,
    val gameId: String,
    val tournamentId: String,
    val matchNumber: Int,
    val predictedHomeGoals: Int,
    val predictedAwayGoals: Int,
    val points: Int
)

fun GameScore.toResultSign(): ResultSign = when {
    homeGoals > awayGoals -> ResultSign.HOME_WIN
    homeGoals < awayGoals -> ResultSign.AWAY_WIN
    else -> ResultSign.DRAW
}

fun GamePrediction.toPredictedScore(): GameScore =
    GameScore(
        homeGoals = predictedHomeGoals,
        awayGoals = predictedAwayGoals
    )

fun GamePrediction.predictedResultSign(): ResultSign =
    toPredictedScore().toResultSign()
