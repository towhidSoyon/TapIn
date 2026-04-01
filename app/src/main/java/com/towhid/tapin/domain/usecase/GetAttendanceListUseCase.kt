package com.towhid.tapin.domain.usecase

import com.towhid.tapin.domain.model.AttendanceRecord
import com.towhid.tapin.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.Flow

class GetAttendanceListUseCase(
    private val repository: AttendanceRepository
) {
    fun execute(): Flow<List<AttendanceRecord>> {
        return repository.getAllEntries()
    }
}
