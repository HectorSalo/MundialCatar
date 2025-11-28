package com.skysam.hchirinos.mundialcatar.dataclass

import java.util.*

/**
 * Created by Hector Chirinos on 03/05/2022.
 */

enum class MatchStage {
 GROUP,
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
 val score: GameScore?      // null si aún n
)

enum class ResultSign {
 HOME_WIN,
 DRAW,
 AWAY_WIN
}

/**
 * Resultado final del partido, desde el punto de vista de la app de pronósticos:
 * - En grupos, si hay empate en goles, usamos DRAW (no hay penales).
 * - En eliminatorias, si hay empate en goles y wentToPenalties = true,
 *   decidimos por penales quién ganó (HOME_WIN o AWAY_WIN).
 */
fun Game.resultSignOrNull(): ResultSign? {
 val s = score ?: return null

 return when {
  s.homeGoals > s.awayGoals -> ResultSign.HOME_WIN
  s.homeGoals < s.awayGoals -> ResultSign.AWAY_WIN
  else -> {
   // empate en goles
   if (stage == MatchStage.GROUP) {
    ResultSign.DRAW
   } else if (s.wentToPenalties && s.homePenalties != null && s.awayPenalties != null) {
    when {
     s.homePenalties > s.awayPenalties -> ResultSign.HOME_WIN
     s.homePenalties < s.awayPenalties -> ResultSign.AWAY_WIN
     else -> null // rarísimo, pero lo dejamos por seguridad
    }
   } else {
    // si por diseño llegara a haber empate sin penales en eliminatoria
    ResultSign.DRAW
   }
  }
 }
}





