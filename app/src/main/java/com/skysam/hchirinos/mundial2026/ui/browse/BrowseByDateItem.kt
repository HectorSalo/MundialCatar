package com.skysam.hchirinos.mundial2026.ui.browse

import com.skysam.hchirinos.mundial2026.dataclass.Game
import com.skysam.hchirinos.mundial2026.dataclass.MatchScheduleSlot

/**
 * Elemento que el ViewModel de [BrowseByDateFragment] expone en su lista
 * unificada. Cada item es un `Game` real o un `MatchScheduleSlot` oficial
 * (cuando todavía no existe el Game).
 *
 * El merge en el VM garantiza que para un mismo `matchNumber` se devuelva
 * el `GameItem` y no el `SlotItem`.
 */
sealed class BrowseByDateItem {
    data class GameItem(val game: Game) : BrowseByDateItem()
    data class SlotItem(val slot: MatchScheduleSlot) : BrowseByDateItem()
}
