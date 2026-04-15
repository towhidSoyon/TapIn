package com.towhid.tapin.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.towhid.tapin.db.AppDatabase
import com.towhid.tapin.domain.model.AttendanceRecord
import com.towhid.tapin.domain.repository.AttendanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AttendanceRepositoryImpl(
    db: AppDatabase
) : AttendanceRepository {

    private val queries = db.appDatabaseQueries
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    override suspend fun insertRecord(date: LocalDate, checkInTime: LocalTime, isLate: Boolean) {
        withContext(Dispatchers.IO) {
            queries.insertEntry(
                date = date.format(dateFormatter),
                checkInTime = checkInTime.format(timeFormatter),
                isLate = isLate
            )
        }
    }

    override suspend fun updateCheckOutTime(date: LocalDate, checkOutTime: LocalTime) {
        withContext(Dispatchers.IO) {
            val record = getRecordByDate(date)
            record?.let {
                queries.updateExitTime(
                    checkOutTime = checkOutTime.format(timeFormatter),
                    id = it.id
                )
            }
        }
    }

    override suspend fun getRecordByDate(date: LocalDate): AttendanceRecord? {
        val dateStr = date.format(dateFormatter)
        return withContext(Dispatchers.IO) {
            queries.getEntryByDate(dateStr).executeAsOneOrNull()?.toDomainModel()
        }
    }

    override fun getAllEntries(): Flow<List<AttendanceRecord>> {
        return queries.getAllEntries()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomainModel() } }
    }

    override fun getMonthlyEntries(month: String): Flow<List<AttendanceRecord>> {
        // month format: yyyy-MM
        return queries.getEntriesByMonth("$month%")
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomainModel() } }
    }

    private fun com.towhid.tapin.db.Attendance.toDomainModel(): AttendanceRecord {
        return AttendanceRecord(
            id = id,
            date = LocalDate.parse(date, dateFormatter),
            checkInTime = LocalTime.parse(checkInTime, timeFormatter),
            checkOutTime = checkOutTime?.let { LocalTime.parse(it, timeFormatter) },
            isLate = isLate
        )
    }
}
