package com.skysam.hchirinos.mundialcatar.dataclass

import java.util.*

/**
 * Created by Hector Chirinos on 03/05/2022.
 */

data class Game(
 val id: String,
 val team1: String,
 val team2: String,
 val date: Date,
 var goalsTeam1: Int = 0,
 var goalsTeam2: Int = 0,
 val round: String,
 val number: Int,
 var started: Boolean
)
