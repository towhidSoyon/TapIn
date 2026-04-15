package com.towhid.tapin.domain.model

import java.time.LocalTime

data class SettingsModel(
    val officeStartTime: LocalTime,
    val lateThresholdTime: LocalTime,
    val allowedLateDays: Int
)
