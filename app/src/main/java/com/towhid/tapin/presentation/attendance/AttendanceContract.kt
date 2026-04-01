package com.towhid.tapin.presentation.attendance

import com.towhid.tapin.domain.model.AttendanceRecord
import java.time.LocalTime

data class AttendanceState(
    val attendanceList: List<AttendanceRecord> = emptyList(),
    val todayCheckInTime: LocalTime? = null,
    val todayCheckOutTime: LocalTime? = null,
    val isLateToday: Boolean = false,
    val monthlyLateCount: Int = 0,
    val shouldDeductLeave: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class AttendanceEvent {
    object OnCheckInClicked : AttendanceEvent()
    object OnCheckOutClicked : AttendanceEvent()
    object LoadData : AttendanceEvent()
    object ClearError : AttendanceEvent()
}
