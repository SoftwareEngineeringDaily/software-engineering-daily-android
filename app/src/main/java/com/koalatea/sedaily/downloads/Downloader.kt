package com.koalatea.sedaily.downloads

import android.os.Environment
import android.util.Log
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject
import java.io.File

data class DownloadEpisodeEvent(
    val progress: Int? = null,
    val episodeId: String? = null
)

class Downloader {
    companion object {
        val downloadingFiles = mutableMapOf<String, PublishSubject<Int>>()
        val currentDownloadProgress: PublishSubject<DownloadEpisodeEvent> = PublishSubject.create()

        fun getDirectoryForEpisodes(): String {
            val dirString = "/sedaily-mp3s/"
            val dirFile = File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), dirString)
            if (!dirFile.exists()) dirFile.mkdirs()

            return dirFile.absolutePath + "/"
        }

        fun downloadMp3(url: String, episodeId: String): PublishSubject<Int>? {
            if (downloadingFiles.containsKey(url)) return null

            downloadingFiles[episodeId] = PublishSubject.create()

            val downloadTask = DownloadTask(object: DownloadTaskEventListener {
                override fun onProgressUpdate(progress: Int?, downloadId: String) {
                    Log.v("keithtest", Thread.currentThread().name)

                    GlobalScope.launch {

                    }


                }
            }, episodeId)
            downloadTask.execute(url, episodeId + ".mp3")

            return downloadingFiles[episodeId]
        }

        fun handleProgressUpdate(progress: Int?, downloadId: String) {
            if (progress != null) {
                DownloadNotification.setProgress(progress)
                downloadingFiles[downloadId]?.onNext(progress)
                currentDownloadProgress.onNext(DownloadEpisodeEvent(progress, downloadId))
                if (progress == 100) {
                    DownloadNotification.hide()
                    DownloadRepository.createDownload(episodeId, url)
                }
            }
        }
    }
}