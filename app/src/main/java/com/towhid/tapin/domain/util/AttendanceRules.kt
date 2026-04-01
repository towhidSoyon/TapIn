package com.towhid.tapin.domain.util

import java.time.LocalTime
import com.towhid.tapin.domain.model.AttendanceRecord

object AttendanceRules {
    private val OFFICE_START_TIME = LocalTime.of(10, 0)
    private val GRACE_PERIOD_TIME = LocalTime.of(10, 30)

    fun isLate(checkInTime: LocalTime): Boolean {
        return checkInTime.isAfter(GRACE_PERIOD_TIME)
    }

    fun countLateDaysForMonth(entries: List<AttendanceRecord>): Int {
        return entries.count { it.isLate }
    }

    fun shouldDeductLeave(lateCount: Int): Boolean {
        return lateCount >= 3
    }
}
