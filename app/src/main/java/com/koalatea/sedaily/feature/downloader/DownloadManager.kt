package com.koalatea.sedaily.feature.downloader

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import androidx.annotation.MainThread
import java.io.File
import java.io.IOException

private const val DIRECTORY_EPISODES = "Episodes"

class DownloadManager(
        private val context: Context
) {

    @MainThread
    fun downloadEpisode(fileName: String, url: String, notificationTitle: String): Long? {
        val downloadDirectory = context.getExternalFilesDir(DIRECTORY_EPISODES) ?: throw IOException("External storage not available")
        val file: File? = File(downloadDirectory, fileName)

        val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(notificationTitle)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setVisibleInDownloadsUi(false)
                .setDestinationUri(Uri.fromFile(file))
                .setAllowedOverMetered(true)
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager

        return downloadManager?.enqueue(request)
    }

    @MainThread
    fun getDownloadStatus(downloadId: Long): DownloadStatus {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        downloadManager?.let {
            val query = DownloadManager.Query().setFilterById(downloadId)

            val cursor = downloadManager.query(query).apply {
                moveToFirst()
            }

            val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            val uri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
            val reason = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))
            val bytesDownloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            val bytesTotal = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
            val progress = bytesDownloaded * 100f / bytesTotal

            cursor.close()

            return when(status) {
                DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.Downloaded(uri)
                DownloadManager.STATUS_FAILED -> DownloadStatus.Error(reason)
                DownloadManager.STATUS_RUNNING -> DownloadStatus.Downloading(progress)
                DownloadManager.STATUS_PAUSED -> DownloadStatus.Downloading(progress)
                DownloadManager.STATUS_PENDING -> DownloadStatus.Downloading(progress)
                else -> DownloadStatus.Unknown
            }
        }

        return DownloadStatus.Unknown
    }

    @MainThread
    fun deleteDownload(fileName: String): Boolean? {
        val downloadDirectory = context.getExternalFilesDir(DIRECTORY_EPISODES) ?: throw IOException("External storage not available")
        val file: File? = File(downloadDirectory, fileName)

        return file?.delete()
    }

}