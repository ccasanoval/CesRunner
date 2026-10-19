package com.cesoft.cesrunner.tracking

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.ui.MainActivity

object TrackingNotification {
    private const val NOTIFICATION_CHANNEL_ID = "CesRunnerChannel"
    const val CANCEL_TRACKING = "com.cesoft.cesrunner.CANCEL_TRACKING"

    fun generateNotification(
        context: Context,
        notificationManager: NotificationManager,
    ): Notification {
        val titleText = context.getString(R.string.app_name)
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID, titleText, NotificationManager.IMPORTANCE_DEFAULT)
        notificationChannel.enableVibration(false)
        notificationChannel.importance = NotificationManager.IMPORTANCE_DEFAULT
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(notificationChannel)
        val bigTextStyle = NotificationCompat.BigTextStyle().setBigContentTitle(titleText)


        val launchActivityIntent = Intent(context, MainActivity::class.java)
        launchActivityIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val activityPendingIntent = PendingIntent.getActivity(
            context, 0, launchActivityIntent, PendingIntent.FLAG_IMMUTABLE)

        val notificationCompatBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        return notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            //TODO: Show current tracking data with setContentText()
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_input_add,
                context.getString(R.string.open),
                activityPendingIntent
            )
            .build()
    }
}
