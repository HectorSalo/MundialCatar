package com.skysam.hchirinos.mundialcatar.dataclass

/**
 * Created by Hector Chirinos on 07/05/2022.
 */

data class Team(
    val id: String,
    val tournamentId: String,
    val code: String,
    val name: String,
    val shortName: String,
    val group: String?,
    val confederation: String?,
    val flagCode: String
)
