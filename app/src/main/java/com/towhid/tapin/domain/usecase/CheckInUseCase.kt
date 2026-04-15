package com.towhid.tapin.domain.usecase

import com.towhid.tapin.domain.model.AttendanceError
import com.towhid.tapin.domain.repository.AttendanceRepository
import com.towhid.tapin.domain.repository.SettingsRepository
import com.towhid.tapin.domain.util.AttendanceRules
import com.towhid.tapin.domain.util.TimeProvider
import kotlinx.coroutines.flow.first

class CheckInUseCase(
    private val repository: AttendanceRepository,
    private val timeProvider: TimeProvider,
    private val settingsRepository: SettingsRepository,
    private val fixMissingCheckOutUseCase: FixMissingCheckOutUseCase
) {
    suspend fun execute(): Result<Unit> {
        // Fix any missing check-outs from previous days before allowing a new check-in
        fixMissingCheckOutUseCase.execute()

        val today = timeProvider.getCurrentDate()

        val existingRecord = repository.getRecordByDate(today)
        if (existingRecord != null) {
            return Result.failure(AttendanceError.AlreadyCheckedIn)
        }

        val settings = settingsRepository.getSettings().first()
        val now = timeProvider.getCurrentTime()
        val isLate = AttendanceRules.isLate(now, settings.lateThresholdTime)

        repository.insertRecord(
            date = today,
            checkInTime = now,
            isLate = isLate
        )
        return Result.success(Unit)
    }
}
