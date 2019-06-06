package com.koalatea.sedaily.database.table

import android.os.Build
import android.text.Html
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

const val EPISODE_DATE_FORMAT = "yyyy-MM-dd\'T\'HH:mm:ss"

@Entity
data class Episode(
        @field:PrimaryKey
        val _id: String,
        val mp3: String?,
        val title: Title?,
        val content: Content?,
        val excerpt: Excerpt?,
        val featuredImage: String?,
        val date: String?,
        val score: Int?,
        val upvoted: Boolean?,
        val bookmarked: Boolean?,
        val thread: Thread?
) {

    var searchQueryHashCode: Int? = null

    // to be consistent w/ changing backend order, we need to keep a data like this
    var indexInResponse: Int = -1

    var isDownloaded: Boolean = false

    val titleString: String?
        get() = title?.rendered?.htmlToText()

    val excerptString: String?
        get() = excerpt?.rendered?.htmlToText()

    val httpsFeaturedImageUrl: String?
        get() = featuredImage?.replace(Regex("^http://"), "https://")

    val utcDate: Date?
        get() = date?.toUTCDate(EPISODE_DATE_FORMAT)

    @Ignore
    private fun String.htmlToText(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(this, 0)
        } else {
            Html.fromHtml(this)
        }.toString()
    }

    @Ignore
    private fun String.toUTCDate(format: String): Date? {
        return try {
            val inputFormat = SimpleDateFormat(format, Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("Etc/UTC")

            return inputFormat.parse(this)
        } catch (e: Exception) {
            // FIXME :: Timber log here
            null
        }
    }

}