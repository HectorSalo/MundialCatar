package com.skysam.hchirinos.mundialcatar.common

import java.text.DateFormat
import java.util.*

/**
 * Created by Hector Chirinos on 06/05/2022.
 */

object Common {
 fun convertDateToString(value: Date): String {
  return DateFormat.getDateInstance().format(value)
 }
}