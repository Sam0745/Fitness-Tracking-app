package com.example.fitnesstrackingapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat.startForeground
import androidx.lifecycle.LifecycleService
import com.example.fitnesstrackingapp.R
import com.example.fitnesstrackingapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.fitnesstrackingapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.fitnesstrackingapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.fitnesstrackingapp.other.Constants.ACTION_STOP_SERVICE
import com.example.fitnesstrackingapp.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.fitnesstrackingapp.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.fitnesstrackingapp.other.Constants.NOTIFICATION_ID
import com.example.fitnesstrackingapp.ui.MainActivity
import timber.log.Timber

class TrackingService : LifecycleService() {
    lateinit var context: Context
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    Timber.d("Started or resumed service")
                }

                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                }

                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
}

private fun startForegroundService(context: Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        createNotificationChannel(notificationManager)
    }

    val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false)
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
        .setContentTitle(context.getString(R.string.running_app))
        .setContentText("00:00:00")
        .setContentIntent(getMainActivityPendingIntent(context))

    // Start the service as a foreground service
//    startForeground(NOTIFICATION_ID, notificationBuilder.build())
}

private fun getMainActivityPendingIntent(context: Context) = PendingIntent.getActivity(
    context,
    0,
    Intent(context, MainActivity::class.java).also {
        it.action = ACTION_SHOW_TRACKING_FRAGMENT
    },
    FLAG_UPDATE_CURRENT
)

@RequiresApi(Build.VERSION_CODES.O)
private fun createNotificationChannel(notificationManager: NotificationManager) {
    val channel = NotificationChannel(
        NOTIFICATION_CHANNEL_ID,
        NOTIFICATION_CHANNEL_NAME,
        IMPORTANCE_LOW
    )
    notificationManager.createNotificationChannel(channel)
}