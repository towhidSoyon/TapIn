package com.towhid.tapin.domain.usecase

import com.towhid.tapin.domain.model.MonthlyStats
import com.towhid.tapin.domain.repository.AttendanceRepository
import com.towhid.tapin.domain.repository.SettingsRepository
import com.towhid.tapin.domain.util.AttendanceRules
import com.towhid.tapin.domain.util.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.format.DateTimeFormatter

class GetMonthlyStatsUseCase(
    private val repository: AttendanceRepository,
    private val timeProvider: TimeProvider,
    private val settingsRepository: SettingsRepository
) {
    fun execute(): Flow<MonthlyStats> {
        val today = timeProvider.getCurrentDate()
        val currentMonthPrefix = today.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        
        return combine(
            repository.getMonthlyEntries(currentMonthPrefix),
            settingsRepository.getSettings()
        ) { entries, settings ->
            // Extra safety filter to ensure same year and month
            val filteredEntries = entries.filter { 
                it.date.month == today.month && it.date.year == today.year 
            }
            
            val lateCount = AttendanceRules.countLateDaysForMonth(filteredEntries)
            MonthlyStats(
                lateCount = lateCount,
                shouldDeductLeave = AttendanceRules.shouldDeductLeave(lateCount, settings.allowedLateDays)
            )
        }
    }
}
