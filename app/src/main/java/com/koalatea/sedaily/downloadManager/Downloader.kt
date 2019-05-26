package com.koalatea.sedaily.downloadManager

import android.os.Environment
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

data class DownloadEpisodeEvent(
        val progress: Int? = null,
        val episodeId: String? = null,
        val url: String? = null
)

data class DownloadQueueItem(
        val episodeId: String? = null,
        val url: String? = null
)

class Downloader {
    companion object {
        val downloadingFiles = mutableMapOf<String, PublishSubject<Int>>()
        val currentDownloadProgress: PublishSubject<DownloadEpisodeEvent> = PublishSubject.create()
        private var downloadTask: DownloadTask? = null
        private val downloadQueue: ArrayList<DownloadQueueItem> = ArrayList()

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

            if (downloadTask == null) {
                downloadTask = DownloadTask(object : DownloadTaskEventListener {
                    override fun onProgressUpdate(progress: Int?, downloadId: String, url: String) {
                        GlobalScope.launch {
                            handleProgressUpdate(progress, downloadId, url)
                        }
                    }
                }, episodeId, url)
                downloadTask?.execute(url, episodeId + ".mp3")
            } else {
                downloadQueue.add(DownloadQueueItem(episodeId, url))
            }


            return downloadingFiles[episodeId]
        }

        fun handleProgressUpdate(progress: Int?, downloadId: String, url: String) {
            if (progress != null) {
                var progressCurrent = progress

                DownloadNotification.setProgress(progressCurrent)
                downloadingFiles[downloadId]?.onNext(progressCurrent)

                // @TODO: HAck progress goes 99 after 100 for some reasons
                if (progress == 99) progressCurrent = 100

                GlobalScope.launch(Dispatchers.Main) {
                    val downloadEvent = DownloadEpisodeEvent(progressCurrent, downloadId, url)
                    currentDownloadProgress.onNext(downloadEvent)
                }

                if (progressCurrent == 100) {
                    downloadingFiles.remove(downloadId)
                    DownloadNotification.hide()
                    DownloadRepository.createDownload(downloadId, Downloader.getDirectoryForEpisodes() + downloadId + ".mp3")

                    if (downloadQueue.size > 0) {
                        val newTask = downloadQueue.removeAt(0)
                        downloadTask = DownloadTask(object : DownloadTaskEventListener {
                            override fun onProgressUpdate(progress: Int?, downloadId: String, url: String) {
                                GlobalScope.launch {
                                    handleProgressUpdate(progress, downloadId, url)
                                }
                            }
                        }, newTask.episodeId!!, url = newTask.url!!)
                        downloadTask?.execute(newTask.url, newTask.episodeId + ".mp3")
                    } else {
                        downloadTask = null
                    }
                }
            }
        }
    }
}