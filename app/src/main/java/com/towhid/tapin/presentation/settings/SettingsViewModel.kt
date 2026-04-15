package com.towhid.tapin.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.towhid.tapin.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime

/**
 * State representing the UI of the Settings Screen.
 * Stores local edits before they are persisted to DataStore.
 */
data class SettingsState(
    val officeStartTime: LocalTime = LocalTime.of(10, 0),
    val lateThresholdTime: LocalTime = LocalTime.of(10, 30),
    val allowedLateDays: Int = 2,
    val isLoading: Boolean = false
)

/**
 * User actions/events for the Settings Screen.
 */
sealed class SettingsEvent {
    data class OnOfficeTimeChanged(val time: LocalTime) : SettingsEvent()
    data class OnLateThresholdChanged(val time: LocalTime) : SettingsEvent()
    data class OnAllowedDaysChanged(val days: Int) : SettingsEvent()
    object OnSaveClicked : SettingsEvent()
}

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    /**
     * Loads the persisted settings from the repository and initializes the UI state.
     */
    private fun loadSettings() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            // Use first() to get the current values once, allowing local edits afterwards.
            settingsRepository.getSettings().first().let { settings ->
                _state.update {
                    it.copy(
                        officeStartTime = settings.officeStartTime,
                        lateThresholdTime = settings.lateThresholdTime,
                        allowedLateDays = settings.allowedLateDays,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Entry point for all user interactions.
     */
    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnOfficeTimeChanged -> {
                _state.update { it.copy(officeStartTime = event.time) }
            }
            is SettingsEvent.OnLateThresholdChanged -> {
                _state.update { it.copy(lateThresholdTime = event.time) }
            }
            is SettingsEvent.OnAllowedDaysChanged -> {
                _state.update { it.copy(allowedLateDays = event.days) }
            }
            is SettingsEvent.OnSaveClicked -> {
                saveSettings()
            }
        }
    }

    /**
     * Persists the current local state to the DataStore.
     */
    private fun saveSettings() {
        viewModelScope.launch {
            val currentState = _state.value
            settingsRepository.updateOfficeStartTime(currentState.officeStartTime)
            settingsRepository.updateLateThresholdTime(currentState.lateThresholdTime)
            settingsRepository.updateAllowedLateDays(currentState.allowedLateDays)
        }
    }
}
