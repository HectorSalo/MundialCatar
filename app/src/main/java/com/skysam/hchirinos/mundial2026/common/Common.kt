package com.skysam.hchirinos.mundial2026.common

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.skysam.hchirinos.mundial2026.dataclass.Game
import com.skysam.hchirinos.mundial2026.dataclass.MatchStage
import java.text.DateFormat
import java.util.*

/**
 * Created by Hector Chirinos on 06/05/2022.
 */

object Common {
 fun convertDateTimeToString(value: Date): String {
  return DateFormat
   .getDateTimeInstance(
    DateFormat.MEDIUM, // fecha
    DateFormat.SHORT   // hora sin segundos
   )
   .format(value)
 }

 fun convertDateToString(value: Date): String {
  return DateFormat.getDateInstance().format(value)
 }

 fun closeKeyboard(view: View) {
  val imn =
   Mundial.Mundial.getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  imn.hideSoftInputFromWindow(view.windowToken, 0)
 }

 fun formatRound(game: Game): String =
  when (game.stage) {
   MatchStage.GROUP -> when (game.group) {
    "A" -> Constants.GROUP_A
    "B" -> Constants.GROUP_B
    "C" -> Constants.GROUP_C
    "D" -> Constants.GROUP_D
    "E" -> Constants.GROUP_E
    "F" -> Constants.GROUP_F
    "G" -> Constants.GROUP_G
    "H" -> Constants.GROUP_H
    "I" -> Constants.GROUP_I
    "J" -> Constants.GROUP_J
    "K" -> Constants.GROUP_K
    "L" -> Constants.GROUP_L
    else -> "Fase de grupos"
   }
   MatchStage.ROUND_OF_32 -> Constants.ROUND_OF_32
   MatchStage.ROUND_OF_16 -> Constants.ROUND_OF_16
   MatchStage.QUARTER_FINAL -> Constants.ROUND_OF_8
   MatchStage.SEMI_FINAL -> Constants.SEMIFINAL
   MatchStage.THIRD_PLACE -> Constants.THIRD_PLACE
   MatchStage.FINAL -> Constants.FINAL
  }
}