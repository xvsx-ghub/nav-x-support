package com.wiswm.nav.support.data.remote.stomp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.core.net.toUri
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.wiswm.nav.support.R
import java.util.concurrent.TimeUnit

class ServiceTimeoutNotificationWorker (
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        showNotificationWithSound(applicationContext)
        return Result.success()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNotificationWithSound(context: Context) {
        val channelId = TAG + "_channelId"
        val channelName = TAG + "_channelName"
        val soundUri = "android.resource://${context.packageName}/${R.raw.notification}".toUri()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(soundUri, audioAttributes)
                enableVibration(true)
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.please_open_the_app_to_keep_your_data_syncing_fast))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setSound(soundUri)
        }

        NotificationManagerCompat.from(context).notify(1001, builder.build())
    }

    companion object Companion {
        const val TAG = "ServiceTimeoutNotificationWorker"

        fun start(context: Context) {
            val request: OneTimeWorkRequest =
                OneTimeWorkRequest.Builder(ServiceTimeoutNotificationWorker::class.java)
                    .addTag(TAG)
                    .setInitialDelay(5, TimeUnit.HOURS)
                    .build()
            WorkManager.getInstance(context).enqueue(request)
        }

        fun stop(context: Context) {
            WorkManager.getInstance(context)
                .cancelAllWorkByTag(TAG)
        }
    }
}