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
    var CANCEL_TRACKING: String = ""

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
        //notificationChannel.setSound(null, null)
        notificationManager.createNotificationChannel(notificationChannel)
        val bigTextStyle = NotificationCompat.BigTextStyle().setBigContentTitle(titleText)

//        val cancelIntent = Intent(context, TrackingService::class.java)
//        cancelIntent.putExtra(CANCEL_TRACKING, true)
//        val servicePendingIntent = PendingIntent.getService(
//            context, 0, cancelIntent,
//            PendingIntent.FLAG_IMMUTABLE + PendingIntent.FLAG_UPDATE_CURRENT
//        )

        val launchActivityIntent = Intent(context, MainActivity::class.java)
        launchActivityIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val activityPendingIntent = PendingIntent.getActivity(
            context, 0, launchActivityIntent, PendingIntent.FLAG_MUTABLE)

        val notificationCompatBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        return notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
//            .setContentText(mainNotificationText)
            //.setSmallIcon(R.drawable.smallicon)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            //.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                android.R.drawable.ic_menu_agenda,
                context.getString(R.string.back),
                activityPendingIntent
            )
//            .addAction(
//                android.R.drawable.ic_menu_close_clear_cancel,
//                context.getString(R.string.stop_tracking),
//                servicePendingIntent
//            )
            .build()
    }
}
