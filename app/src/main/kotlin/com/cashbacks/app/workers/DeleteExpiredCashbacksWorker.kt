package com.cashbacks.app.workers

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.cashbacks.app.BuildConfig
import com.cashbacks.app.R
import com.cashbacks.app.ui.MainActivity
import com.cashbacks.common.composables.usePermissions
import com.cashbacks.common.utils.now
import com.cashbacks.common.utils.parseToDate
import com.cashbacks.features.cashback.domain.usecase.DeleteCashbacksUseCase
import com.cashbacks.features.cashback.domain.usecase.GetExpiredCashbacksUseCase
import com.cashbacks.features.settings.domain.model.Settings
import com.cashbacks.features.settings.domain.usecase.GetSettingsUseCase
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
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


    @SuppressLint("MissingPermission")
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
                            com.cashbacks.common.resources.R.plurals.expired_cashbacks_deletion_success,
                            count,
                            count
                        )
                        val notification = createNotification(msg)
                        val id = generateIdFromDate()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            usePermissions(
                                Manifest.permission.POST_NOTIFICATIONS,
                                context = applicationContext,
                                grantPermission = {}
                            ) {
                                pushNotification(id, notification)
                            }
                        } else {
                            pushNotification(id, notification)
                        }
                        Log.i(TAG, msg)
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


    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(0, createNotification("Empty notification"))
    }


    private fun getOrCreateNotificationChannel(): NotificationChannel {
        val notificationManager = NotificationManagerCompat.from(applicationContext)

        val channelId = applicationContext
            .getString(R.string.default_notification_channel_id)
        val channel = notificationManager.getNotificationChannel(channelId)
        if (channel != null) {
            return channel
        }

        val newChannel = NotificationChannel(
            channelId,
            applicationContext.getString(R.string.default_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(newChannel)
        return newChannel
    }


    private fun generateIdFromDate(date: LocalDate = LocalDate.now()): Int {
        val appBuildDate = BuildConfig.VERSION_DATE.parseToDate()
        val daysNumber = (date - appBuildDate).days
        return daysNumber.coerceAtLeast(0)
    }



    private fun createNotification(message: String): Notification {
        val channel = getOrCreateNotificationChannel()

        val mainActivityIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntentFlag = PendingIntent.FLAG_IMMUTABLE
        val mainActivityPendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            mainActivityIntent,
            pendingIntentFlag
        )

        return Notification.Builder(applicationContext, channel.id)
            .setSmallIcon(com.cashbacks.common.resources.R.drawable.cashback_filled)
            .setLargeIcon(Icon.createWithResource(applicationContext, R.drawable.icon))
            .setContentTitle(applicationContext.getString(R.string.activity_name))
            .setContentText(message)
            .let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    it.setAllowSystemGeneratedContextualActions(true)
                } else {
                    it
                }
            }
            .setContentIntent(mainActivityPendingIntent)
            .setAutoCancel(true)
            .setColorized(true)
            .setColor(0xff6A00)
            .setCategory(Notification.CATEGORY_EVENT)
            .setShowWhen(true)
            .build()
    }


    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun pushNotification(id: Int, notification: Notification) {
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.notify(id, notification)
    }
}