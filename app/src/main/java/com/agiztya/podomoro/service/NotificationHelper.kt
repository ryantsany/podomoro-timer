package com.agiztya.podomoro.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.agiztya.podomoro.MainActivity
import com.agiztya.podomoro.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID_ONGOING = "timer_ongoing"
        // Changed ID to force Android to recognize the new sound setting
        const val CHANNEL_ID_FINISHED = "timer_finished_v2" 
        const val NOTIFICATION_ID_ONGOING = 1
        const val NOTIFICATION_ID_FINISHED = 2
    }

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Ongoing timer channel (silent, low priority)
            val ongoingChannel = NotificationChannel(
                CHANNEL_ID_ONGOING,
                "Timer Running",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows the current timer countdown"
                setShowBadge(false)
            }

            // Construct URI for the custom sound in res/raw
            val soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/" + R.raw.timer_finished)

            // Finished timer channel (high priority with custom sound)
            val finishedChannel = NotificationChannel(
                CHANNEL_ID_FINISHED,
                "Timer Finished",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when the timer completes"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 200, 1000, 200, 1000, 200, 1000)
                // Set sound on the channel (Required for API 26+)
                setSound(
                    soundUri,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }

            notificationManager.createNotificationChannels(listOf(ongoingChannel, finishedChannel))
        }
    }

    fun buildOngoingNotification(
        timeRemaining: Long,
        isRunning: Boolean,
        timerMode: String,
        taskName: String,
        totalDuration: Int
    ): Notification {
        val minutes = (timeRemaining / 1000) / 60
        val seconds = (timeRemaining / 1000) % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)

        val title = if (taskName.isNotBlank() && timerMode == "Focus") {
            "$taskName session"
        } else {
            "$timerMode session"
        }

        val contentText = if (totalDuration > 0) {
            "Running for $totalDuration minutes • $timeString remaining"
        } else {
            timeString
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_ONGOING)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (isRunning) {
            val pauseIntent = createActionIntent(TimerService.ACTION_PAUSE)
            builder.addAction(
                R.drawable.ic_launcher_foreground,
                "Pause",
                pauseIntent
            )
        } else {
            val resumeIntent = createActionIntent(TimerService.ACTION_RESUME)
            builder.addAction(
                R.drawable.ic_launcher_foreground,
                "Resume",
                resumeIntent
            )
        }

        val stopIntent = createActionIntent(TimerService.ACTION_STOP)
        builder.addAction(
            R.drawable.ic_launcher_foreground,
            "Stop",
            stopIntent
        )

        return builder.build()
    }

    fun showFinishedNotification(timerMode: String) {
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/" + R.raw.timer_finished)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_FINISHED)
            .setContentTitle("$timerMode Complete!")
            .setContentText(getCompletionMessage(timerMode))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri) // Set sound on builder for compatibility
            .build()

        notificationManager.notify(NOTIFICATION_ID_FINISHED, notification)
    }

    fun updateOngoingNotification(
        timeRemaining: Long,
        isRunning: Boolean,
        timerMode: String,
        taskName: String,
        totalDuration: Int
    ) {
        val notification = buildOngoingNotification(timeRemaining, isRunning, timerMode, taskName, totalDuration)
        notificationManager.notify(NOTIFICATION_ID_ONGOING, notification)
    }

    fun cancelOngoingNotification() {
        notificationManager.cancel(NOTIFICATION_ID_ONGOING)
    }

    fun cancelFinishedNotification() {
        notificationManager.cancel(NOTIFICATION_ID_FINISHED)
    }

    private fun createActionIntent(action: String): PendingIntent {
        val intent = Intent(context, TimerService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getCompletionMessage(timerMode: String): String {
        return when {
            timerMode.contains("Focus", ignoreCase = true) -> "Great work! Time for a break."
            timerMode.contains("Break", ignoreCase = true) -> "Break's over! Ready to focus?"
            else -> "Timer completed!"
        }
    }
}
