package com.skysam.hchirinos.mundialcatar.ui.gameday

import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameToView

/**
 * Created by Hector Chirinos on 08/09/2023.
 */

interface OnClick {
 fun select(game: GameToView)
}