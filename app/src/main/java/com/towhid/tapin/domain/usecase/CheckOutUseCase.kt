package com.towhid.tapin.domain.usecase

import com.towhid.tapin.domain.model.AttendanceError
import com.towhid.tapin.domain.repository.AttendanceRepository
import com.towhid.tapin.domain.util.TimeProvider

class CheckOutUseCase(
    private val repository: AttendanceRepository,
    private val timeProvider: TimeProvider
) {
    suspend fun execute(): Result<Unit> {
        val today = timeProvider.getCurrentDate()
        
        val existingRecord = repository.getRecordByDate(today)
            ?: return Result.failure(AttendanceError.NotCheckedIn)

        if (existingRecord.checkOutTime != null) {
            return Result.failure(AttendanceError.AlreadyCheckedOut)
        }

        val now = timeProvider.getCurrentTime()
        repository.updateCheckOutTime(today, now)
        
        return Result.success(Unit)
    }
}
