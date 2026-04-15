package com.towhid.tapin.domain.model

sealed class AttendanceError(override val message: String) : Exception() {
    object NotCheckedIn : AttendanceError("Please check in first")
    object AlreadyCheckedOut : AttendanceError("Already checked out for today")
    object AlreadyCheckedIn : AttendanceError("Already checked in for today")
}
