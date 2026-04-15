package com.towhid.tapin.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.towhid.tapin.domain.repository.SettingsRepository
import com.towhid.tapin.domain.usecase.FixMissingCheckOutUseCase
import com.towhid.tapin.domain.usecase.GetAttendanceListUseCase
import com.towhid.tapin.domain.usecase.GetMonthlyStatsUseCase
import com.towhid.tapin.domain.util.TimeProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * State representing the UI of the Dashboard/Statistics Screen.
 */
data class DashboardState(
    val lateCount: Int = 0,
    val allowedLateDays: Int = 2,
    val remainingLateDays: Int = 0,
    val shouldDeductLeave: Boolean = false,
    val totalPresent: Int = 0,
    val attendancePercentage: Float = 0f,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class DashboardViewModel(
    private val getAttendanceListUseCase: GetAttendanceListUseCase,
    private val getMonthlyStatsUseCase: GetMonthlyStatsUseCase,
    private val settingsRepository: SettingsRepository,
    private val fixMissingCheckOutUseCase: FixMissingCheckOutUseCase,
    private val timeProvider: TimeProvider
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            fixMissingCheckOutUseCase.execute()
            loadDashboardData()
        }
    }

    /**
     * Combines multiple data sources to provide a unified dashboard state.
     * Logic for calculations is centralized here to keep the UI clean.
     */
    private fun loadDashboardData() {
        combine(
            getAttendanceListUseCase.execute(),
            getMonthlyStatsUseCase.execute(),
            settingsRepository.getSettings()
        ) { attendanceList, monthlyStats, settings ->
            val totalPresent = attendanceList.size
            val today = timeProvider.getCurrentDate()
            val daysElapsedInMonth = today.dayOfMonth
            
            // Basic calculation: total working days is considered as days passed so far in the month
            val percentage = if (daysElapsedInMonth > 0) {
                (totalPresent.toFloat() / daysElapsedInMonth.toFloat()) * 100f
            } else 0f

            DashboardState(
                lateCount = monthlyStats.lateCount,
                allowedLateDays = settings.allowedLateDays,
                remainingLateDays = (settings.allowedLateDays - monthlyStats.lateCount).coerceAtLeast(0),
                shouldDeductLeave = monthlyStats.shouldDeductLeave,
                totalPresent = totalPresent,
                attendancePercentage = percentage,
                isLoading = false,
                errorMessage = null
            )
        }
        .catch { e ->
            _state.update { 
                it.copy(
                    isLoading = false, 
                    errorMessage = e.message ?: "Failed to load dashboard data" 
                ) 
            }
        }
        .onEach { newState ->
            _state.value = newState
        }
        .launchIn(viewModelScope)
    }
}
