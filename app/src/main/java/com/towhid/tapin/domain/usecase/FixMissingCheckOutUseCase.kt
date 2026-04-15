package com.towhid.tapin.domain.usecase

import com.towhid.tapin.domain.repository.AttendanceRepository
import com.towhid.tapin.domain.util.TimeProvider
import kotlinx.coroutines.flow.first
import java.time.LocalTime

class FixMissingCheckOutUseCase(
    private val repository: AttendanceRepository,
    private val timeProvider: TimeProvider
) {
    suspend fun execute() {
        val today = timeProvider.getCurrentDate()
        val allEntries = repository.getAllEntries().first()
        
        val missingCheckOuts = allEntries.filter { 
            it.checkOutTime == null && it.date.isBefore(today)
        }

        missingCheckOuts.forEach { record ->
            repository.updateCheckOutTime(record.date, LocalTime.of(23, 59))
        }
    }
}
