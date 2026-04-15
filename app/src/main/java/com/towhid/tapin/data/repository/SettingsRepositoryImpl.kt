package com.towhid.tapin.data.repository

import com.towhid.tapin.data.datastore.SettingsDataStore
import com.towhid.tapin.domain.model.SettingsModel
import com.towhid.tapin.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SettingsRepositoryImpl(
    private val dataStore: SettingsDataStore
) : SettingsRepository {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun getSettings(): Flow<SettingsModel> {
        return dataStore.data.map { preferences ->
            SettingsModel(
                officeStartTime = preferences[SettingsDataStore.OFFICE_START_TIME]?.let { 
                    LocalTime.parse(it, timeFormatter) 
                } ?: LocalTime.of(10, 0),
                lateThresholdTime = preferences[SettingsDataStore.LATE_THRESHOLD_TIME]?.let { 
                    LocalTime.parse(it, timeFormatter) 
                } ?: LocalTime.of(10, 30),
                allowedLateDays = preferences[SettingsDataStore.ALLOWED_LATE_DAYS] ?: 2
            )
        }
    }

    override suspend fun updateOfficeStartTime(time: LocalTime) {
        dataStore.saveOfficeStartTime(time.format(timeFormatter))
    }

    override suspend fun updateLateThresholdTime(time: LocalTime) {
        dataStore.saveLateThresholdTime(time.format(timeFormatter))
    }

    override suspend fun updateAllowedLateDays(days: Int) {
        dataStore.saveAllowedLateDays(days)
    }
}
