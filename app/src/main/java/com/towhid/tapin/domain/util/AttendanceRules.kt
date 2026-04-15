package com.towhid.tapin.domain.util

import java.time.LocalTime
import com.towhid.tapin.domain.model.AttendanceRecord

object AttendanceRules {

    fun isLate(checkInTime: LocalTime, lateThreshold: LocalTime): Boolean {
        return checkInTime.isAfter(lateThreshold)
    }

    fun countLateDaysForMonth(entries: List<AttendanceRecord>): Int {
        return entries.count { it.isLate }
    }

    fun shouldDeductLeave(lateCount: Int, allowedLates: Int): Boolean {
        return lateCount > allowedLates
    }
}
