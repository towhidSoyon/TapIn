package com.towhid.tapin.presentation.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.towhid.tapin.domain.model.AttendanceRecord
import com.towhid.tapin.domain.usecase.*
import com.towhid.tapin.domain.util.TimeProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime

/**
 * ViewModel responsible for managing attendance state and handling user actions.
 * Follows Clean Architecture by interacting with domain-layer UseCases.
 */
class HomeViewModel(
    private val checkInUseCase: CheckInUseCase,
    private val checkOutUseCase: CheckOutUseCase,
    private val getAttendanceListUseCase: GetAttendanceListUseCase,
    private val getMonthlyStatsUseCase: GetMonthlyStatsUseCase,
    private val fixMissingCheckOutUseCase: FixMissingCheckOutUseCase,
    private val timeProvider: TimeProvider
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    // Holds the reference to the active data stream collection
    private var dataCollectionJob: Job? = null

    init {
        loadInitialData()
        observeTimeChanges()
    }

    /**
     * Monitors system time to handle date changes while the app is running.
     * Triggers a refresh if the date moves forward (e.g., at midnight).
     */
    private fun observeTimeChanges() {
        viewModelScope.launch {
            while (true) {
                val today = timeProvider.getCurrentDate()
                val currentAttendance = _state.value.attendanceList
                
                if (currentAttendance.isNotEmpty()) {
                    val lastRecordDate = currentAttendance.firstOrNull()?.date
                    if (lastRecordDate != null && lastRecordDate != today) {
                        loadInitialData()
                    }
                }
                delay(60000) // Poll every minute
            }
        }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnCheckInClicked -> performCheckIn()
            is HomeEvent.OnCheckOutClicked -> performCheckOut()
            is HomeEvent.LoadData -> loadInitialData()
            is HomeEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
            is HomeEvent.ClearSuccess -> _state.update { it.copy(successMessage = null) }
        }
    }

    /**
     * Loads the attendance history and monthly statistics.
     * Only displays and calculates data for the current month/year.
     */
    private fun loadInitialData() {
        dataCollectionJob?.cancel()
        val today = timeProvider.getCurrentDate()

        dataCollectionJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Fix any missing check-outs from previous days before loading data
            fixMissingCheckOutUseCase.execute()

            combine(
                getAttendanceListUseCase.execute(),
                getMonthlyStatsUseCase.execute()
            ) { attendanceList, monthlyStats ->
                val todayRecord = attendanceList.find { it.date == today }
                
                _state.update {
                    it.copy(
                        attendanceList = attendanceList,
                        todayCheckInTime = todayRecord?.checkInTime,
                        todayCheckOutTime = todayRecord?.checkOutTime,
                        isLateToday = todayRecord?.isLate ?: false,
                        monthlyLateCount = monthlyStats.lateCount,
                        shouldDeductLeave = monthlyStats.shouldDeductLeave,
                        isLoading = false
                    )
                }
            }.catch { e ->
                _state.update { it.copy(errorMessage = e.message, isLoading = false) }
            }.collect()
        }
    }

    private fun performCheckIn() {
        if (_state.value.todayCheckInTime != null) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            checkInUseCase.execute()
                .onSuccess {
                    _state.update { it.copy(successMessage = "Checked in successfully") }
                }
                .onFailure { error ->
                    _state.update { it.copy(errorMessage = error.message ?: "Check-in failed", isLoading = false) }
                }
        }
    }

    private fun performCheckOut() {
        val currentState = _state.value
        if (currentState.todayCheckInTime == null || currentState.todayCheckOutTime != null) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            checkOutUseCase.execute()
                .onSuccess {
                    _state.update { it.copy(successMessage = "Checked out successfully") }
                }
                .onFailure { error ->
                    _state.update { it.copy(errorMessage = error.message ?: "Check-out failed", isLoading = false) }
                }
        }
    }
}

data class HomeState(
    val attendanceList: List<AttendanceRecord> = emptyList(),
    val todayCheckInTime: LocalTime? = null,
    val todayCheckOutTime: LocalTime? = null,
    val isLateToday: Boolean = false,
    val monthlyLateCount: Int = 0,
    val shouldDeductLeave: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

sealed class HomeEvent {
    object OnCheckInClicked : HomeEvent()
    object OnCheckOutClicked : HomeEvent()
    object LoadData : HomeEvent()
    object ClearError : HomeEvent()
    object ClearSuccess : HomeEvent()
}
