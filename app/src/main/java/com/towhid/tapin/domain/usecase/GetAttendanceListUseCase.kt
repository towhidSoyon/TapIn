package com.towhid.tapin.domain.usecase

import com.towhid.tapin.domain.model.AttendanceRecord
import com.towhid.tapin.domain.repository.AttendanceRepository
import com.towhid.tapin.domain.util.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetAttendanceListUseCase(
    private val repository: AttendanceRepository,
    private val timeProvider: TimeProvider
) {
    fun execute(): Flow<List<AttendanceRecord>> {
        val today = timeProvider.getCurrentDate()
        return repository.getAllEntries().map { entries ->
            entries.filter { 
                it.date.month == today.month && it.date.year == today.year 
            }
        }
    }
}
