package com.towhid.tapin.presentation.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.towhid.tapin.domain.usecase.*
import com.towhid.tapin.domain.util.TimeProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class AttendanceViewModel(
    private val checkInUseCase: CheckInUseCase,
    private val checkOutUseCase: CheckOutUseCase,
    private val getAttendanceListUseCase: GetAttendanceListUseCase,
    private val getMonthlyStatsUseCase: GetMonthlyStatsUseCase,
    private val timeProvider: TimeProvider
) : ViewModel() {

    private val _state = MutableStateFlow(AttendanceState())
    val state: StateFlow<AttendanceState> = _state.asStateFlow()

    init {
        onEvent(AttendanceEvent.LoadData)
    }

    fun onEvent(event: AttendanceEvent) {
        when (event) {
            is AttendanceEvent.OnCheckInClicked -> checkIn()
            is AttendanceEvent.OnCheckOutClicked -> checkOut()
            is AttendanceEvent.LoadData -> loadData()
            is AttendanceEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadData() {
        val today = timeProvider.getCurrentDate()
        val currentMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"))

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Combine attendance list and monthly stats
            combine(
                getAttendanceListUseCase.execute(),
                getMonthlyStatsUseCase.execute(currentMonth)
            ) { list, stats ->
                val todayRecord = list.find { it.date == today }
                
                _state.update {
                    it.copy(
                        attendanceList = list,
                        todayCheckInTime = todayRecord?.checkInTime,
                        todayCheckOutTime = todayRecord?.checkOutTime,
                        isLateToday = todayRecord?.isLate ?: false,
                        monthlyLateCount = stats.lateCount,
                        shouldDeductLeave = stats.shouldDeductLeave,
                        isLoading = false
                    )
                }
            }.collect()
        }
    }

    private fun checkIn() {
        viewModelScope.launch {
            checkInUseCase.execute()
                .onSuccess {
                    onEvent(AttendanceEvent.LoadData)
                }
                .onFailure { error ->
                    _state.update { it.copy(errorMessage = error.message ?: "Check-in failed") }
                }
        }
    }

    private fun checkOut() {
        viewModelScope.launch {
            checkOutUseCase.execute()
                .onSuccess {
                    onEvent(AttendanceEvent.LoadData)
                }
                .onFailure { error ->
                    _state.update { it.copy(errorMessage = error.message ?: "Check-out failed") }
                }
        }
    }
}
