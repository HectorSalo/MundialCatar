package com.skysam.hchirinos.mundialcatar.dataclass

/**
 * Created by Hector Chirinos in the home office on 8 dic. 2025
 */
data class KnockoutSlot(
    val stage: MatchStage,   // ROUND_OF_32, ROUND_OF_16, etc.
    val matchNumber: Int,
    val homeSource: SeedDescriptor,
    val awaySource: SeedDescriptor
)

sealed class SeedDescriptor {
    data class GroupPosition(val group: String, val position: Int) : SeedDescriptor()
    data class BestThird(val rank: Int) : SeedDescriptor()            // 1..8, según ranking global de terceros
    data class WinnerOfMatch(val stage: MatchStage, val matchNumber: Int) : SeedDescriptor()
}
