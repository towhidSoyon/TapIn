package com.towhid.tapin.domain.usecase

import com.towhid.tapin.domain.model.MonthlyStats
import com.towhid.tapin.domain.repository.AttendanceRepository
import com.towhid.tapin.domain.util.AttendanceRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetMonthlyStatsUseCase(
    private val repository: AttendanceRepository
) {
    fun execute(month: String): Flow<MonthlyStats> {
        return repository.getMonthlyEntries(month).map { entries ->
            val lateCount = AttendanceRules.countLateDaysForMonth(entries)
            MonthlyStats(
                lateCount = lateCount,
                shouldDeductLeave = AttendanceRules.shouldDeductLeave(lateCount)
            )
        }
    }
}
