package com.skysam.hchirinos.mundialcatar.dataclass

import java.util.*

/**
 * Created by Hector Chirinos on 11/05/2022.
 */

data class GameUser(
 val team1: String,
 val team2: String,
 val date: Date,
 var goals1: Int = 0,
 var goals2: Int = 0,
 var flag1: String = "",
 var flag2: String = "",
 val round: String,
 val number: Int,
 var points: Int = 0,
 val predict: Boolean = false
)
