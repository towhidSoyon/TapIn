package com.towhid.tapin.domain.usecase

import com.towhid.tapin.domain.model.AttendanceError
import com.towhid.tapin.domain.repository.AttendanceRepository
import com.towhid.tapin.domain.util.AttendanceRules
import com.towhid.tapin.domain.util.TimeProvider

class CheckInUseCase(
    private val repository: AttendanceRepository,
    private val timeProvider: TimeProvider
) {
    suspend fun execute(): Result<Unit> {
        val today = timeProvider.getCurrentDate()

        val existingRecord = repository.getRecordByDate(today)
        if (existingRecord != null) {
            return Result.failure(AttendanceError.AlreadyCheckedIn)
        }

        val now = timeProvider.getCurrentTime()
        val isLate = AttendanceRules.isLate(now)

        repository.insertRecord(
            date = today,
            checkInTime = now,
            isLate = isLate
        )
        return Result.success(Unit)
    }
}
