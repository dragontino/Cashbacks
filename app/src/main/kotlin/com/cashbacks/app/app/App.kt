package com.cashbacks.app.app

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.cashbacks.app.di.ApplicationModules
import com.cashbacks.app.workers.DeleteExpiredCashbacksWorker
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import java.time.Duration

class App : Application() {
    private val deleteExpiredCashbacksWork = PeriodicWorkRequestBuilder<DeleteExpiredCashbacksWorker>(
        repeatInterval = Duration.ofDays(1),
        flexTimeInterval = Duration.ofMinutes(15)
    ).build()


    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            androidLogger()

            modules(ApplicationModules)
        }

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            uniqueWorkName = DeleteExpiredCashbacksWorker.NAME,
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
            request = deleteExpiredCashbacksWork
        )
    }
}