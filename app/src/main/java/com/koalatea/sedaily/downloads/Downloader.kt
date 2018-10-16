package com.koalatea.sedaily.downloads

import android.os.Environment
import io.reactivex.subjects.PublishSubject
import java.io.File

class Downloader {
    companion object {
        val downloadingFiles = mutableMapOf<String, PublishSubject<Int>>()

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

            DownloadRepository.createDownload(episodeId, url)

            val downloadTask = DownloadTask(object: DownloadTaskEventListener {
                override fun onProgressUpdate(progress: Int?, downloadId: String) {
                    downloadingFiles[downloadId]?.onNext(progress!!)
                }
            }, episodeId)
            downloadTask.execute(url, episodeId + ".mp3")

            return downloadingFiles[episodeId]
        }
    }
}