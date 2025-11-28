package com.skysam.hchirinos.mundialcatar.common

/**
 * Created by Hector Chirinos on 06/05/2022.
 */

object Constants {
 // =========================
 // Colecciones Firestore
 // =========================
 const val GAMES = "games"
 const val TEAMS = "teams"
 const val PREDICT = "predict" // colección de pronósticos (legacy, luego la revisamos)
 const val INFO_APP = "infoApp"

 // =========================
 // Campos comunes
 // =========================
 const val TOURNAMENT_ID = "tournamentId"
 const val CREATED_AT = "createdAt"
 const val UPDATED_AT = "updatedAt"
 const val ID_USER = "idUser"

 // =========================
 // Campos de Game (nuevo modelo)
 // =========================
 const val DATE = "date"                // Timestamp del partido
 const val HOME_TEAM_ID = "homeTeamId"  // id del equipo local
 const val AWAY_TEAM_ID = "awayTeamId"  // id del equipo visitante
 const val STAGE = "stage"              // nombre de la fase (enum en código)
 const val GROUP = "group"              // grupo A, B... o null en eliminatorias
 const val MATCH_NUMBER = "matchNumber" // número interno de partido
 const val STATUS = "status"            // SCHEDULED / FINISHED
 const val START = "start"              // bool para “ya empezó” (si lo sigues usando)

 const val HOME_GOALS = "homeGoals"
 const val AWAY_GOALS = "awayGoals"
 const val WENT_TO_PENALTIES = "wentToPenalties"
 const val HOME_PENALTIES = "homePenalties"
 const val AWAY_PENALTIES = "awayPenalties"

 // =========================
 // Campos de Team (nuevo modelo)
 // =========================
 const val CODE = "code"                // código del equipo (ARG, BRA, etc.)
 const val NAME = "name"                // nombre (puede usarse también para usuario/app)
 const val SHORT_NAME = "shortName"     // nombre corto
 const val CONFEDERATION = "confederation"
 const val FLAG_CODE = "flagCode"       // clave para drawable local

 // =========================
 // Campos de Team legacy (stats viejas)
 // Los mantengo para que el proyecto compile por ahora.
 // Luego, cuando terminemos de migrar, se pueden borrar.
 // =========================
 const val WINS = "wins"
 const val DEFEATS = "defeats"
 const val TIED = "tied"
 const val GOALS_CONCEDED = "goalsConceded"
 const val GOALS_MADE = "goalsMade"
 const val POINTS = "points"
 const val FLAG = "flag" // antiguo campo de bandera (string/URL)

 // =========================
 // Campos de usuario / perfil
 // =========================
 const val IMAGE = "image"
 const val EMAIL = "email"

 // =========================
 // Textos de grupos (para UI)
 // =========================
 const val GROUP_A = "Grupo A"
 const val GROUP_B = "Grupo B"
 const val GROUP_C = "Grupo C"
 const val GROUP_D = "Grupo D"
 const val GROUP_E = "Grupo E"
 const val GROUP_F = "Grupo F"
 const val GROUP_G = "Grupo G"
 const val GROUP_H = "Grupo H"

 // =========================
 // Textos de rondas (para UI)
 // =========================
 const val OCTAVOS = "Octavos"
 const val CUARTOS = "Cuartos"
 const val SEMIFINAL = "Semifinal"
 const val FINAL = "Final"

 // =========================
 // Usuarios especiales (admin / test)
 // =========================
 const val USER_MAIN = "hectorsalomonchirinos@gmail.com"
 const val USER_TEST = "test@gmail.com"

 // =========================
 // Preferencias
 // =========================
 const val PREFERENCES = "preferences"
 const val PREFERENCES_NOTIFICATION = "notification"

 // =========================
 // Info app
 // =========================
 const val VERSION_CODE = "versionCode"
 const val VERSION_NAME = "versionName"
}