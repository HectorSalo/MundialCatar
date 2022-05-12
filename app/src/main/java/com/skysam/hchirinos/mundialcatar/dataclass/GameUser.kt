package com.skysam.hchirinos.mundialcatar.dataclass

import java.util.*

/**
 * Created by Hector Chirinos on 11/05/2022.
 */

data class GameUser(
 val id: String,
 val team1: String,
 val team2: String,
 val flag1: String,
 val flag2: String,
 val date: Date,
 var goalsTeam1: Int = 0,
 var goalsTeam2: Int = 0,
 var penal1: Int = 0,
 var penal2: Int = 0,
 val round: String,
 val number: Int,
 val points: Int = 0
)
