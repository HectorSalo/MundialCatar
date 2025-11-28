package com.skysam.hchirinos.mundialcatar.dataclass

import com.google.firebase.Timestamp

data class TeamEntity(
    val tournamentId: String = "",
    val code: String = "",
    val name: String = "",
    val shortName: String = "",
    val group: String? = null,
    val confederation: String? = null,
    val flagCode: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
