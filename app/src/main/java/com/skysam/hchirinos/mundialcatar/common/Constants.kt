package com.skysam.hchirinos.mundialcatar.common

/**
 * Created by Hector Chirinos on 06/05/2022.
 */

object Constants {

    // =========================
    // Colecciones Firestore
    // =========================
    const val GAMES = "games"
    const val GAMES_USERS = "gamesUsers"
    const val USERS = "users"
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
    const val GAME_ID = "gameId"            // id del partido
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
    const val NAME = "name"                // nombre (puede usarse también para usuario/app)

    // =========================
    // Campos de Team legacy (stats viejas)
    // Los mantengo para que el proyecto compile por ahora.
    // Luego, cuando terminemos de migrar, se pueden borrar.
    // =========================
    const val POINTS = "points"
    const val GOALS1 = "goals1"
    const val GOALS2 = "goals2"
    const val NUMBER = "number"

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
    const val GROUP_I = "Grupo I"
    const val GROUP_J = "Grupo J"
    const val GROUP_K = "Grupo K"
    const val GROUP_L = "Grupo L"

    // =========================
    // Textos de rondas (para UI)
    // =========================
    const val ROUND_OF_32 = "Dieciseisavos"
    const val ROUND_OF_16 = "Octavos"
    const val ROUND_OF_8 = "Cuartos"
    const val SEMIFINAL = "Semifinal"
    const val THIRD_PLACE = "Tercer Lugar"
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
    const val WORLD_CUP_2026_ID = "world_cup_2026"
}