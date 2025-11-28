package com.skysam.hchirinos.mundialcatar.common

import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.ScorePrediction
import com.skysam.hchirinos.mundialcatar.dataclass.calculatePointsInScore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScoringRules @Inject constructor() {

    fun calculatePoints(prediction: ScorePrediction, game: Game): Int =
        calculatePointsInScore(prediction, game)
}