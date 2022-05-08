package com.skysam.hchirinos.mundialcatar.dataclass

/**
 * Created by Hector Chirinos on 07/05/2022.
 */

data class Team(
    val id: String,
    val flag: String,
    var points: Int = 0,
    var goalsMade: Int = 0,
    var goalsConceded: Int = 0,
    var wins: Int = 0,
    var defeats: Int = 0,
    var tied: Int = 0,
    val group: String
)
