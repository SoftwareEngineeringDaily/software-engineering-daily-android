package com.koalatea.sedaily.downloadManager

import android.content.Context
import android.content.Intent
import android.os.Build

class DownloaderServiceManager {
    companion object {
        fun startBackgroundDownload(activity: Context, id: String?, url: String?) {
            if (id == null || url == null) return

            val intent = Intent(activity, DownloadIntentService::class.java)

            intent.putExtra("episode-id", id)
            intent.putExtra("episode-url", url)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity.startForegroundService(intent)
            } else {
                activity.startService(intent)
            }
        }
    }
}