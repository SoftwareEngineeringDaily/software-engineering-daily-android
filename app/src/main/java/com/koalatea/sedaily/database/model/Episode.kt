package com.koalatea.sedaily.database.model

import android.os.Parcelable
import androidx.core.text.HtmlCompat
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.koalatea.sedaily.util.EpisodeHtmlUtils
import com.koalatea.sedaily.util.toUTCDate
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
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
) : Parcelable {

    @Suppress("SuspiciousVarProperty")
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

    val titleString: String?
        get() = title?.rendered?.htmlToText()

    val excerptString: String?
        get() = content?.rendered?.let { EpisodeHtmlUtils.removePlayerAndLinksTags(it).htmlToText(consecutive = true) }

    val httpsMp3Url: String?
        get() = mp3?.replace(Regex("^http://"), "https://")

    val httpsFeaturedImageUrl: String?
        get() = featuredImage?.replace(Regex("^http://"), "https://")

    val httpsGuestImageUrl: String?
        get() = guestImage?.replace(Regex("^http://"), "https://")

    val utcDate: Date?
        get() = date?.toUTCDate()

    @Ignore
    private fun String.stripHtml(consecutive: Boolean): String {
        var strippedHtml = this

        if (consecutive) {
            strippedHtml = strippedHtml.replace('\n', ' ')
        }
        strippedHtml = strippedHtml.replace(160.toChar(), ' ')
        strippedHtml = strippedHtml.replace(65532.toChar(), ' ')
        strippedHtml = strippedHtml.trim { it <= ' ' }

        return strippedHtml
    }

    @Ignore
    private fun String.htmlToText(consecutive: Boolean = false): String {
        return HtmlCompat.fromHtml(this, 0).toString().stripHtml(consecutive)
    }

}