package com.koalatea.sedaily.database.model

import android.os.Build
import android.text.Html
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.koalatea.sedaily.util.toUTCDate
import java.util.*

@Entity
data class Episode(
        val _id: String,
        val mp3: String?,
        val title: Title?,
        val content: Content?,
        val excerpt: Excerpt?,
        val featuredImage: String?,
        val guestImage: String?,
        val date: String?,
        val score: Int?,
        val upvoted: Boolean?,
        val bookmarked: Boolean?,
        val thread: Thread?,
        val filterTags: List<Tag>?,
        val link: String,
        val transcriptURL: String?
) {

    @field:PrimaryKey
    var uniqueId: String = _id
        get() = _id + searchQueryHashCode?.toString()

    var searchQueryHashCode: Int? = null

    // to be consistent w/ changing backend order, we need to keep a data like this
    var indexInResponse: Int = -1

    @Ignore
    var downloadedId: Long? = null

    @Ignore
    var uriString: String? = null

    @Ignore
    var startPosition: Long = 0L

    @Ignore
    var total: Long? = null

    val titleString: String?
        get() = title?.rendered?.htmlToText()

    val excerptString: String?
        get() = excerpt?.rendered?.htmlToText()

    val httpsMp3Url: String?
        get() = mp3?.replace(Regex("^http://"), "https://")

    val httpsFeaturedImageUrl: String?
        get() = featuredImage?.replace(Regex("^http://"), "https://")

    val httpsGuestImageUrl: String?
        get() = guestImage?.replace(Regex("^http://"), "https://")

    val utcDate: Date?
        get() = date?.toUTCDate()

    @Ignore
    private fun String.htmlToText(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(this, 0)
        } else {
            Html.fromHtml(this)
        }.toString()
    }

}