package com.skysam.hchirinos.mundialcatar.dataclass

/**
 * Created by Hector Chirinos on 11/05/2022.
 */

data class GameUser(
 val id: String,
 var idUser: String,
 var goals1: Int = 0,
 var goals2: Int = 0,
 val number: Int,
 var points: Int = 0
)
