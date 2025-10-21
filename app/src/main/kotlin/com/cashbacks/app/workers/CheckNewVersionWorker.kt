package com.cashbacks.app.workers

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.cashbacks.app.BuildConfig
import com.cashbacks.app.R
import com.cashbacks.common.utils.now
import com.cashbacks.common.utils.parseToDate
import com.cashbacks.core.network.NetworkService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.random.Random

class CheckNewVersionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    companion object {
        private const val TAG = "CheckNewVersionWorker"
        const val NAME = "Check new version"
    }

    private val networkService by inject<NetworkService>()

    private val progressStateFlow = MutableStateFlow<Int?>(null)

    override suspend fun doWork(): Result {
        Log.d(TAG, "Work started")
        progressStateFlow.update { 0 }
        return try {
            val latestAppVersion = networkService.getAppLatestVersion()
            val currentAppVersion = getCurrentAppVersionName()
            progressStateFlow.update { 10 }

            if (latestAppVersion <= currentAppVersion) {
                when (latestAppVersion) {
                    currentAppVersion -> Log.d(
                        TAG,
                        "Already the latest app version: $latestAppVersion"
                    )

                    else -> Log.d(TAG, "Current app version is newer than on server ($currentAppVersion vs $latestAppVersion)")
                }
                progressStateFlow.update { 100 }
                return Result.success()
            }

            progressStateFlow.update { 30 }
            val downloadLink = networkService.getAppDownloadLink() ?: networkService.getReleaseUrl()
            progressStateFlow.update { 80 }
            val notification = createDownloadNewVersionNotification(downloadLink)
            val id = generateIdFromDate()
            progressStateFlow.update { 100 }
            pushNotification(id, notification)
            return Result.success()
        } catch (e: Throwable) {
            Log.e(TAG, e.message, e)
            progressStateFlow.update { null }
            Result.retry()
        } finally {
            Log.d(TAG, "Work finished")
        }
    }


    override suspend fun getForegroundInfo(): ForegroundInfo {
        val id = Random.nextInt()
        val notification = createNotification(
            title = "Checking for a new version",
            message = when (val progress = progressStateFlow.value) {
                null -> "Something went wrong"
                else -> "Current progress: $progress"
            }
        )
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(id, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(id, notification)
        }
    }


    private fun getCurrentAppVersionName(): String {
        val fullVersionName = BuildConfig.VERSION_NAME
        return if (BuildConfig.DEBUG) {
            fullVersionName.split("-")[0]
        } else {
            fullVersionName
        }
    }


    private fun getOrCreateNotificationChannel(): NotificationChannel {
        val notificationManager = NotificationManagerCompat.from(applicationContext)

        val channelId = applicationContext
            .getString(R.string.new_version_notification_channel_id)
        val channel = notificationManager.getNotificationChannel(channelId)
        if (channel != null) {
            return channel
        }

        val newChannel = NotificationChannel(
            channelId,
            applicationContext.getString(R.string.new_version_notification_channel_name),
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



    private fun createDownloadNewVersionNotification(downloadLink: String): Notification {
        val downloadIntent = downloadLink.let {
            val intent = Intent(Intent.ACTION_VIEW).apply { data = downloadLink.toUri() }
            PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }
        val title = try {
            applicationContext.getString(R.string.new_version_is_available)
        } catch (_: Resources.NotFoundException) {
            Log.d(TAG, "String resource for notification title isn`t found, use default title")
            "A new version of the app is available"
        }
        val message = try {
            applicationContext.getString(R.string.click_to_download)
        } catch (_: Resources.NotFoundException) {
            Log.d(TAG, "String resource for notification message isn`t found, use default message")
            "Click here to download"
        }
        Log.d(TAG, "$title. $message")
        return createNotification(title, message, downloadIntent)
    }


    private fun createNotification(
        title: String,
        message: String,
        onClickIntent: PendingIntent? = null,
        actions: List<NotificationCompat.Action> = emptyList()
    ): Notification {
        val channel = getOrCreateNotificationChannel()

        return NotificationCompat.Builder(applicationContext, channel.id)
            .setSmallIcon(com.cashbacks.common.resources.R.drawable.cashback_filled)
            .setLargeIcon(
                BitmapFactory.decodeResource(applicationContext.resources, R.drawable.icon)
            )
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(onClickIntent)
            .setAutoCancel(true)
            .let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    it.setAllowSystemGeneratedContextualActions(true)
                } else {
                    it
                }
            }
            .run {
                actions.fold(this) { builder, action -> builder.addAction(action) }
            }
            .setColorized(true)
            .setColor(0xff6A00)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
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