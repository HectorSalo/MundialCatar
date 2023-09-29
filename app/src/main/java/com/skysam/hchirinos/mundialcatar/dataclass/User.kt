package com.skysam.hchirinos.mundialcatar.dataclass

/**
 * Created by Hector Chirinos on 11/05/2022.
 */

data class User(
    val id: String,
    val name: String?,
    val image: String?,
    val email: String?,
    var points: Int = 0
)
