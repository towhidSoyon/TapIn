package com.towhid.tapin.domain.repository

import com.towhid.tapin.domain.model.SettingsModel
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime

interface SettingsRepository {
    fun getSettings(): Flow<SettingsModel>
    suspend fun updateOfficeStartTime(time: LocalTime)
    suspend fun updateLateThresholdTime(time: LocalTime)
    suspend fun updateAllowedLateDays(days: Int)
}
