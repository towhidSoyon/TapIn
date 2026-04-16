package com.towhid.tapin.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.towhid.tapin.domain.repository.AttendanceRepository
import com.towhid.tapin.domain.repository.SettingsRepository
import com.towhid.tapin.domain.util.AttendanceRules
import com.towhid.tapin.domain.util.TimeProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * State representing the UI of the Dashboard/Statistics Screen.
 */
data class DashboardState(
    val lateCount: Int = 0,
    val allowedLateDays: Int = 0,
    val remainingLateDays: Int = 0,
    val shouldDeductLeave: Boolean = false,
    val totalPresent: Int = 0,
    val attendancePercentage: Float = 0f,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class DashboardViewModel(
    private val attendanceRepository: AttendanceRepository,
    private val settingsRepository: SettingsRepository,
    private val timeProvider: TimeProvider
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val today = timeProvider.getCurrentDate()
        val currentMonthPrefix = today.format(DateTimeFormatter.ofPattern("yyyy-MM"))

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            fixMissingCheckOuts()

            combine(
                attendanceRepository.getAllEntries().map { entries ->
                    entries.filter { it.date.month == today.month && it.date.year == today.year }
                },
                settingsRepository.getSettings()
            ) { attendanceList, settings ->
                val lateCount = AttendanceRules.countLateDaysForMonth(attendanceList)
                val totalPresent = attendanceList.size
                val workingDaysElapsed = calculateWorkingDaysElapsed(today)
                
                val percentage = if (workingDaysElapsed > 0) {
                    (totalPresent.toFloat() / workingDaysElapsed.toFloat()) * 100f
                } else 0f

                DashboardState(
                    lateCount = lateCount,
                    allowedLateDays = settings.allowedLateDays,
                    remainingLateDays = (settings.allowedLateDays - lateCount).coerceAtLeast(0),
                    shouldDeductLeave = AttendanceRules.shouldDeductLeave(lateCount, settings.allowedLateDays),
                    totalPresent = totalPresent,
                    attendancePercentage = percentage,
                    isLoading = false
                )
            }.catch { e ->
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = e.message ?: "Failed to load dashboard data" 
                    ) 
                }
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    /**
     * Calculates the number of working days (excluding Friday and Saturday) 
     * from the start of the month until the given date.
     */
    private fun calculateWorkingDaysElapsed(today: LocalDate): Int {
        var workingDays = 0
        for (day in 1..today.dayOfMonth) {
            val date = today.withDayOfMonth(day)
            if (date.dayOfWeek != DayOfWeek.FRIDAY && date.dayOfWeek != DayOfWeek.SATURDAY) {
                workingDays++
            }
        }
        return workingDays
    }

    private suspend fun fixMissingCheckOuts() {
        val today = timeProvider.getCurrentDate()
        val allEntries = attendanceRepository.getAllEntries().first()
        val missingCheckOuts = allEntries.filter { 
            it.checkOutTime == null && it.date.isBefore(today)
        }
        missingCheckOuts.forEach { record ->
            attendanceRepository.updateCheckOutTime(record.date, LocalTime.of(23, 59))
        }
    }
}
