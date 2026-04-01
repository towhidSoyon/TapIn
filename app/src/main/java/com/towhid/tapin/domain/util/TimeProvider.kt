package com.towhid.tapin.domain.util

import java.time.LocalDate
import java.time.LocalTime

interface TimeProvider {
    fun getCurrentDate(): LocalDate
    fun getCurrentTime(): LocalTime
}

class DefaultTimeProvider : TimeProvider {
    override fun getCurrentDate(): LocalDate = LocalDate.now()
    override fun getCurrentTime(): LocalTime = LocalTime.now()
}
