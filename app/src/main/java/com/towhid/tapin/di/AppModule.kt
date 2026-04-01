package com.towhid.tapin.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.towhid.tapin.data.repository.AttendanceRepositoryImpl
import com.towhid.tapin.db.AppDatabase
import com.towhid.tapin.domain.repository.AttendanceRepository
import com.towhid.tapin.domain.usecase.*
import com.towhid.tapin.domain.util.DefaultTimeProvider
import com.towhid.tapin.domain.util.TimeProvider
import com.towhid.tapin.presentation.attendance.AttendanceViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<SqlDriver>  {
        AndroidSqliteDriver(
            schema = AppDatabase.Schema,
            context = androidContext(),
            name = "attendance.db"
        )
    }

    single {
        AppDatabase(get())
    }

    single<AttendanceRepository> {
        AttendanceRepositoryImpl(get())
    }

    single<TimeProvider> { DefaultTimeProvider() }

    factory { CheckInUseCase(get(), get()) }
    factory { CheckOutUseCase(get(), get()) }
    factory { GetAttendanceListUseCase(get()) }
    factory { GetMonthlyStatsUseCase(get()) }

    viewModel {
        AttendanceViewModel(get(), get(), get(), get(), get())
    }
}
