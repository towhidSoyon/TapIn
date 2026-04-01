package com.towhid.tapin.domain.model

sealed class AttendanceError : Exception() {
    object NotCheckedIn : AttendanceError()
    object AlreadyCheckedOut : AttendanceError()
    object AlreadyCheckedIn : AttendanceError()
}
