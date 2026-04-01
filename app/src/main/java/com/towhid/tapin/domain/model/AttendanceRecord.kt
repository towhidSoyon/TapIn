package com.towhid.tapin.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class AttendanceRecord(
    val id: Long,
    val date: LocalDate,
    val checkInTime: LocalTime,
    val checkOutTime: LocalTime?,
    val isLate: Boolean
)
