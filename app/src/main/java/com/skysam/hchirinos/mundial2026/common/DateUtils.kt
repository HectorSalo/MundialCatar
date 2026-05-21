package com.skysam.hchirinos.mundial2026.common

import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

object DateUtils {

    fun startOfDay(date: Date, zone: ZoneId = ZoneId.systemDefault()): Date {
        val local = date.toInstant().atZone(zone).toLocalDate()
        return Date.from(local.atStartOfDay(zone).toInstant())
    }

    fun startOfNextDay(date: Date, zone: ZoneId = ZoneId.systemDefault()): Date {
        val local = date.toInstant().atZone(zone).toLocalDate()
        return Date.from(local.plusDays(1).atStartOfDay(zone).toInstant())
    }

    fun isSameDay(a: Date, b: Date, zone: ZoneId = ZoneId.systemDefault()): Boolean {
        val da: LocalDate = a.toInstant().atZone(zone).toLocalDate()
        val db: LocalDate = b.toInstant().atZone(zone).toLocalDate()
        return da == db
    }
}
