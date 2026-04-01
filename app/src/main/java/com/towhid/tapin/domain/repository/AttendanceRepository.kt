package com.towhid.tapin.domain.repository

import com.towhid.tapin.domain.model.AttendanceRecord
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

interface AttendanceRepository {
    suspend fun insertRecord(date: LocalDate, checkInTime: LocalTime, isLate: Boolean)
    suspend fun updateCheckOutTime(date: LocalDate, checkOutTime: LocalTime)
    suspend fun getRecordByDate(date: LocalDate): AttendanceRecord?
    fun getAllEntries(): Flow<List<AttendanceRecord>>
    fun getMonthlyEntries(month: String): Flow<List<AttendanceRecord>>
}
