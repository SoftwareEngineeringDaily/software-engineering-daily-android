package com.koalatea.sedaily.feature.downloader

import com.koalatea.sedaily.model.Download
import com.koalatea.sedaily.model.DownloadDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DownloadRepository constructor(private val downloadDao: DownloadDao) {

    suspend fun createDownload(episodeId: String, url: String) {
        withContext(Dispatchers.IO) {
            val downloadEntry = Download(episodeId, url)
            downloadDao.insertAll(downloadEntry)
        }
    }

    suspend fun getDownloadForId(episodeId: String) = withContext(Dispatchers.IO) {
        downloadDao.findById(episodeId)
    }

    suspend fun removeDownloadForId(episodeId: String) {
        val download = getDownloadForId(episodeId)

        withContext(Dispatchers.Main) {
            download?.let { downloadDao.delete(download) }
        }
    }

}