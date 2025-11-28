package com.skysam.hchirinos.mundialcatar.dataclass

data class ScorePrediction(
    val id: String,
    val userId: String,
    val gameId: String,
    val homeGoals: Int,
    val awayGoals: Int
)

fun ScorePrediction.resultSign(): ResultSign =
    when {
        homeGoals > awayGoals -> ResultSign.HOME_WIN
        homeGoals < awayGoals -> ResultSign.AWAY_WIN
        else -> ResultSign.DRAW
    }


/**
 * Calcula los puntos del usuario para un partido, según:
 *
 * 6 puntos  -> marcador exacto.
 * 3 puntos  -> acierta quién gana (o empate), pero falla el marcador.
 * -2 puntos -> resultado incorrecto normal.
 * -3 puntos -> acierta el marcador exacto pero al revés (ej: 2-1 vs 1-2).
 */
fun calculatePointsInScore(
    prediction: ScorePrediction,
    game: Game
): Int {
    val score = game.score ?: return 0 // si el partido no se ha jugado, 0 puntos
    val realSign = game.resultSignOrNull() ?: return 0
    val predSign = prediction.resultSign()

    val predHome = prediction.homeGoals
    val predAway = prediction.awayGoals
    val realHome = score.homeGoals
    val realAway = score.awayGoals

    // 1) Marcador exacto
    if (predHome == realHome && predAway == realAway) {
        return 6
    }

    // 2) Marcador espejo (mismas cifras, ganador contrario)
    val isMirror = predHome == realAway && predAway == realHome
    if (isMirror && predSign != realSign) {
        // mismo marcador pero al revés
        return -3
    }

    // 3) Resultado correcto (signo correcto)
    if (predSign == realSign) {
        return 3
    }

    // 4) Resultado incorrecto normal
    return -2
}


