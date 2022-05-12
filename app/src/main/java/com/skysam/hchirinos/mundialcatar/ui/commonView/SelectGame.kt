package com.skysam.hchirinos.mundialcatar.ui.commonView

import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameUser

/**
 * Created by Hector Chirinos on 09/05/2022.
 */

interface SelectGame {
 fun updateResult(game: Game)
 fun updatePredict(gameUser: GameUser)
}