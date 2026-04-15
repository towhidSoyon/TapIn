package com.towhid.tapin.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.towhid.tapin.data.datastore.SettingsDataStore
import com.towhid.tapin.data.repository.AttendanceRepositoryImpl
import com.towhid.tapin.data.repository.SettingsRepositoryImpl
import com.towhid.tapin.db.AppDatabase
import com.towhid.tapin.domain.repository.AttendanceRepository
import com.towhid.tapin.domain.repository.SettingsRepository
import com.towhid.tapin.domain.usecase.*
import com.towhid.tapin.domain.util.DefaultTimeProvider
import com.towhid.tapin.domain.util.TimeProvider
import com.towhid.tapin.presentation.attendance.HomeViewModel
import com.towhid.tapin.presentation.dashboard.DashboardViewModel
import com.towhid.tapin.presentation.settings.SettingsViewModel
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

    single { SettingsDataStore(androidContext()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }

    single<TimeProvider> { DefaultTimeProvider() }

    factory { FixMissingCheckOutUseCase(get(), get()) }
    factory { CheckInUseCase(get(), get(), get(), get()) }
    factory { CheckOutUseCase(get(), get(), get()) }
    factory { GetAttendanceListUseCase(get(), get()) }
    factory { GetMonthlyStatsUseCase(get(), get(), get()) }

    viewModel {
        HomeViewModel(get(), get(), get(), get(), get(), get())
    }

    viewModel {
        DashboardViewModel(get(), get(), get(), get(),get())
    }

    viewModel {
        SettingsViewModel(get())
    }
}
