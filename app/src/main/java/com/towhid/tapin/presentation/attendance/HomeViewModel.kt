package com.towhid.tapin.presentation.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.towhid.tapin.domain.model.AttendanceError
import com.towhid.tapin.domain.model.AttendanceRecord
import com.towhid.tapin.domain.repository.AttendanceRepository
import com.towhid.tapin.domain.repository.SettingsRepository
import com.towhid.tapin.domain.util.AttendanceRules
import com.towhid.tapin.domain.util.TimeProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.collections.filter
import kotlin.collections.forEach

/**
 * ViewModel responsible for managing attendance state and handling user actions.
 * Refactored to remove UseCase layer and interact directly with repositories.
 */
class HomeViewModel(
    private val repository: AttendanceRepository,
    private val settingsRepository: SettingsRepository,
    private val timeProvider: TimeProvider
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private var dataCollectionJob: Job? = null

    init {
        loadInitialData()
        observeTimeChanges()
    }

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
                delay(60000)
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

    private fun loadInitialData() {
        dataCollectionJob?.cancel()
        val today = timeProvider.getCurrentDate()
        val currentMonthPrefix = today.format(DateTimeFormatter.ofPattern("yyyy-MM"))

        dataCollectionJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            fixMissingCheckOuts()

            combine(
                repository.getAllEntries().map { entries ->
                    entries.filter { it.date.month == today.month && it.date.year == today.year }
                },
                combine(
                    repository.getMonthlyEntries(currentMonthPrefix),
                    settingsRepository.getSettings()
                ) { entries, settings ->
                    val filteredEntries = entries.filter {
                        it.date.month == today.month && it.date.year == today.year
                    }
                    val lateCount = AttendanceRules.countLateDaysForMonth(filteredEntries)
                    Pair(lateCount, AttendanceRules.shouldDeductLeave(lateCount, settings.allowedLateDays))
                }
            ) { attendanceList, stats ->
                val todayRecord = attendanceList.find { it.date == today }

                _state.update {
                    it.copy(
                        attendanceList = attendanceList,
                        todayCheckInTime = todayRecord?.checkInTime,
                        todayCheckOutTime = todayRecord?.checkOutTime,
                        isLateToday = todayRecord?.isLate ?: false,
                        monthlyLateCount = stats.first,
                        shouldDeductLeave = stats.second,
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
            try {
                fixMissingCheckOuts()
                val today = timeProvider.getCurrentDate()
                val existingRecord = repository.getRecordByDate(today)
                if (existingRecord != null) {
                    throw AttendanceError.AlreadyCheckedIn
                }

                val settings = settingsRepository.getSettings().first()
                val now = timeProvider.getCurrentTime()
                val isLate = AttendanceRules.isLate(now, settings.lateThresholdTime)

                repository.insertRecord(today, now, isLate)
                _state.update { it.copy(successMessage = "Checked in successfully") }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = e.message ?: "Check-in failed", isLoading = false) }
            }
        }
    }

    private fun performCheckOut() {
        val currentState = _state.value
        if (currentState.todayCheckInTime == null || currentState.todayCheckOutTime != null) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                fixMissingCheckOuts()
                val today = timeProvider.getCurrentDate()
                val existingRecord = repository.getRecordByDate(today) ?: throw AttendanceError.NotCheckedIn
                if (existingRecord.checkOutTime != null) {
                    throw AttendanceError.AlreadyCheckedOut
                }

                val now = timeProvider.getCurrentTime()
                repository.updateCheckOutTime(today, now)
                _state.update { it.copy(successMessage = "Checked out successfully") }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = e.message ?: "Check-out failed", isLoading = false) }
            }
        }
    }

    private suspend fun fixMissingCheckOuts() {
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