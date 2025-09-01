package com.cashbacks.app.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cashbacks.common.resources.R
import com.cashbacks.common.utils.now
import com.cashbacks.features.cashback.domain.usecase.DeleteCashbacksUseCase
import com.cashbacks.features.cashback.domain.usecase.GetExpiredCashbacksUseCase
import com.cashbacks.features.settings.domain.model.Settings
import com.cashbacks.features.settings.domain.usecase.GetSettingsUseCase
import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DeleteExpiredCashbacksWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {
    private companion object {
        const val TAG = "DeleteExpiredCashbacksWorker"
    }

    private val getExpiredCashbacks by inject<GetExpiredCashbacksUseCase>()
    private val deleteCashbacks by inject<DeleteCashbacksUseCase>()
    private val getSettings by inject<GetSettingsUseCase>()


    private fun createNotification(message: String) {
        Log.i(TAG, message)
    }


    override suspend fun doWork(): Result {
        Log.i(TAG, "Work started")
        return try {
            val settings = getSettings().getOrNull() ?: Settings()
            if (settings.autoDeleteExpiredCashbacks.not()) {
                return Result.success()
            }

            val today = LocalDate.now()
            val expiredCashbacks = getExpiredCashbacks(today).getOrNull() ?: emptyList()
            if (expiredCashbacks.isEmpty()) {
                return Result.success()
            } else {
                val deletionResult = deleteCashbacks(expiredCashbacks).onSuccess { count ->
                    if (count > 0) {
                        val msg = applicationContext.resources.getQuantityString(
                            R.plurals.expired_cashbacks_deletion_success,
                            count,
                            count
                        )
                        createNotification(msg)
                    } else {
                        Log.i(TAG, "There are 0 cashbacks to delete.")
                    }
                }
                return when {
                    deletionResult.isSuccess -> Result.success()
                    else -> Result.retry()
                }
            }

        } catch (e: Throwable) {
            Log.e(TAG, e.message, e)
            Result.retry()
        } finally {
            Log.i(TAG, "Work finished")
        }
    }
}