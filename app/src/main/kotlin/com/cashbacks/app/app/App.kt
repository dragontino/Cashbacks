package com.cashbacks.app.app

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.cashbacks.app.di.ApplicationModules
import com.cashbacks.app.workers.CheckNewVersionWorker
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

    private val checkNewVersionWorkRequest: PeriodicWorkRequest
        get() {
            val constraints = Constraints.Builder()
                .setRequiresCharging(true)
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()

            return PeriodicWorkRequestBuilder<CheckNewVersionWorker>(
                repeatInterval = Duration.ofDays(2),
                flexTimeInterval = Duration.ofHours(2)
            )
                .setConstraints(constraints)
                .build()
        }


    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            androidLogger()

            modules(ApplicationModules)
        }


        val workManager = WorkManager.getInstance(this)
        workManager.enqueueUniquePeriodicWork(
            uniqueWorkName = DeleteExpiredCashbacksWorker.NAME,
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
            request = deleteExpiredCashbacksWork
        )

        workManager.enqueueUniquePeriodicWork(
            uniqueWorkName = CheckNewVersionWorker.NAME,
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP,
            request = checkNewVersionWorkRequest
        )
    }
}