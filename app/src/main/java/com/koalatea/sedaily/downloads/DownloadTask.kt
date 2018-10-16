package com.koalatea.sedaily.downloads

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

interface DownloadTaskEventListener {
    fun onProgressUpdate(progress: Int?, downloadId: String)
}

class DownloadTask(
    val downloadListener: DownloadTaskEventListener?,
    val downloadId: String
): AsyncTask<String, Int, String>() {
    override fun doInBackground(vararg args: String): String {
        var input: InputStream? = null
        var output: OutputStream? = null
        var connection: HttpURLConnection? = null

        try
        {
            val url = URL(args[0])
            connection = url.openConnection() as HttpURLConnection
            connection.connect()
            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.responseCode !== HttpURLConnection.HTTP_OK) {
                return ("Server returned HTTP " + connection.responseCode
                        + " " + connection.responseMessage)
            }
            // this will be useful to display download percentage
            // might be -1: server did not report the length
            val fileLength = connection.getContentLength()

            // download the file
            input = connection.getInputStream()
            output = FileOutputStream(Downloader.getDirectoryForEpisodes() + args[1])

            val data = ByteArray(4096)
            var total:Long = 0
            var count: Int

            count = input.read(data)

            while (count != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close()
                    return ""
                }

                total += count.toLong()

                // publishing the progress....
                if (fileLength > 0)
                    publishProgress((total * 100 / fileLength).toInt())
                output.write(data, 0, count)

                count = input.read(data)
            }
        } catch (e:Exception) {
            return e.toString()
        } finally {
            try {
                output?.close()
                input?.close()
            }
            catch (ignored: IOException) {}
            connection?.disconnect()
        }

        return ""
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)

        downloadListener?.onProgressUpdate(values[0], downloadId)
    }
}