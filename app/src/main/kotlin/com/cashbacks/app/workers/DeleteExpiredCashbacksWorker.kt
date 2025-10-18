package com.cashbacks.app.workers

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.cashbacks.app.BuildConfig
import com.cashbacks.app.R
import com.cashbacks.app.ui.MainActivity
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
    companion object {
        private const val TAG = "DeleteExpiredCashbacksWorker"
        const val NAME = "Delete expired cashbacks"
    }

    private val getExpiredCashbacks by inject<GetExpiredCashbacksUseCase>()
    private val deleteCashbacks by inject<DeleteCashbacksUseCase>()
    private val getSettings by inject<GetSettingsUseCase>()


    override suspend fun doWork(): Result {
        Log.d(TAG, "Work started")
        return try {
            val settings = getSettings().getOrNull() ?: Settings()
            if (settings.autoDeleteExpiredCashbacks.not()) {
                Log.d(TAG, "Auto deleting cashbacks is disabled in settings")
                return Result.success()
            }

            val today = LocalDate.now()
            val expiredCashbacks = getExpiredCashbacks(today).getOrNull() ?: emptyList()
            if (expiredCashbacks.isEmpty()) {
                Log.d(TAG, "There are 0 cashbacks to delete")
                return Result.success()
            }

            val deletionResult = deleteCashbacks(expiredCashbacks).onSuccess { count ->
                if (count > 0) {
                    val msg = try {
                        applicationContext.resources.getQuantityString(
                            com.cashbacks.common.resources.R.plurals.expired_cashbacks_deletion_success,
                            count,
                            count
                        )
                    } catch (_: Resources.NotFoundException) {
                        Log.d(TAG, "Resource 'R.plurals.expired_cashbacks_deletion_success' isn`t found, use default message")
                        "$count expired cashbacks have been deleted"
                    }
                    val notification = createNotification(msg)
                    val id = generateIdFromDate()
                    pushNotification(id, notification)
                    Log.d(TAG, msg)
                }
                if (count != expiredCashbacks.size) {
                    Log.d(TAG, "There were ${expiredCashbacks.size} cashbacks to delete but $count cashbacks have been deleted")
                }
            }

            return when {
                deletionResult.getOrNull() == expiredCashbacks.size -> Result.success()
                else -> Result.retry()
            }

        } catch (e: Throwable) {
            Log.e(TAG, e.message, e)
            Result.retry()
        } finally {
            Log.d(TAG, "Work finished")
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


    private fun pushNotification(id: Int, notification: Notification) {
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }
        notificationManager.notify(id, notification)
    }
}