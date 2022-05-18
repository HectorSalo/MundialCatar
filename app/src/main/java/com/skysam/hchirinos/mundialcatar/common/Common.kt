package com.skysam.hchirinos.mundialcatar.common

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.text.DateFormat
import java.util.*

/**
 * Created by Hector Chirinos on 06/05/2022.
 */

object Common {
 fun convertDateTimeToString(value: Date): String {
  return DateFormat.getDateTimeInstance().format(value)
 }

 fun convertDateToString(value: Date): String {
  return DateFormat.getDateInstance().format(value)
 }

 fun convertDateToDateTimeZone(value: Date): Date {
  val date1 = DateFormat.getDateTimeInstance().format(value)
  return DateFormat.getDateInstance().parse(date1)!!
 }

 fun closeKeyboard(view: View) {
  val imn = Mundial.Mundial.getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  imn.hideSoftInputFromWindow(view.windowToken, 0)
 }
}