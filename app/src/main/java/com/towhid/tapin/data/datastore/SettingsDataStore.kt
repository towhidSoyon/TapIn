package com.towhid.tapin.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val OFFICE_START_TIME = stringPreferencesKey("office_start_time")
        val LATE_THRESHOLD_TIME = stringPreferencesKey("late_threshold_time")
        val ALLOWED_LATE_DAYS = intPreferencesKey("allowed_late_days")
    }

    suspend fun saveOfficeStartTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[OFFICE_START_TIME] = time
        }
    }

    suspend fun saveLateThresholdTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[LATE_THRESHOLD_TIME] = time
        }
    }

    suspend fun saveAllowedLateDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[ALLOWED_LATE_DAYS] = days
        }
    }

    fun getOfficeStartTime(): Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[OFFICE_START_TIME]
    }

    fun getLateThresholdTime(): Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[LATE_THRESHOLD_TIME]
    }

    fun getAllowedLateDays(): Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[ALLOWED_LATE_DAYS]
    }
    
    val data: Flow<Preferences> = context.dataStore.data
}
