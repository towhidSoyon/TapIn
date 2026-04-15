package com.towhid.tapin.domain.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object Formatter {
    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
    private val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH)

    fun formatTime(time: LocalTime?): String {
        return time?.format(timeFormatter) ?: "N/A"
    }

    fun formatDate(date: LocalDate?): String {
        return date?.format(dateFormatter) ?: "N/A"
    }
}
