package com.towhid.tapin

import android.app.Application
import com.towhid.tapin.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class TapInApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@TapInApp)
            modules(appModule)
        }
    }
}