package com.koalatea.sedaily.downloadManager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.koalatea.sedaily.R
import com.koalatea.sedaily.SEDApp


class DownloadNotification {
    companion object {
        private var notificationId: Int = 8443344
        private var mBuilder: NotificationCompat.Builder? = null
        private var mNotifyManager: NotificationManager? = null

        init {
            this.notificationId = notificationId
            val context = SEDApp.appContext
            val notificationMessage = "Downloading your queued podcasts"
            val notificationIntent = Intent(context, DownloadIntentService::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)
            mNotifyManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

            val CHANNEL_ID = "sedaily-music"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = context?.getString(R.string.app_name)
                val importance = NotificationManager.IMPORTANCE_LOW
                val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
                mNotifyManager?.createNotificationChannel(mChannel)
            }

            mBuilder = NotificationCompat.Builder(context!!, CHANNEL_ID)
                    .setContentTitle(context?.getString(R.string.downloading_title))
                    .setContentText(notificationMessage)
                    .setSmallIcon(R.drawable.sedaily_logo)
                    .setContentIntent(pendingIntent)
                    .setProgress(0, 0, true)
        }

        fun show(): Notification? {
            val notification = mBuilder?.build()
            mNotifyManager?.notify(notificationId, notification)
            return notification
        }

        fun setProgress(progress: Int) {
            mBuilder?.setProgress(100, progress, false)
            mNotifyManager?.notify(notificationId, mBuilder?.build())
        }

        fun hide() {
            mBuilder?.setContentText("Download complete")
            mBuilder?.setProgress(0, 0, false)
            mNotifyManager?.notify(notificationId, mBuilder?.build())
        }
    }
}