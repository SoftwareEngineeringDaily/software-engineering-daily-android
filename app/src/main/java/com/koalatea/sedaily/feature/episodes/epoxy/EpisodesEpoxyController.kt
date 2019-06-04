package com.koalatea.sedaily.feature.episodes.epoxy

import android.os.Build
import android.text.Html
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.koalatea.sedaily.model.Episode
import java.text.SimpleDateFormat
import java.util.*

const val EPISODE_DATE_FORMAT = "yyyy-MM-dd\'T\'HH:mm:ss"

class EpisodesEpoxyController(
        private val upvoteClickListener: (id: String) -> Unit,
        private val commentClickListener: (id: String) -> Unit,
        private val bookmarkClickListener: (id: String) -> Unit,
        private val episodeClickListener: (id: String) -> Unit
) : PagedListEpoxyController<Episode>() {

    override fun buildItemModel(currentPosition: Int, item: Episode?): EpoxyModel<*> {
        return if (item == null) {
            EpisodeEpoxyModelWithHolder_()
                    .id(-currentPosition)
                    .title("loading $currentPosition")
        } else {
            val imageHttpsUrl = item.featuredImage?.replace(Regex("^http://"), "https://")

            EpisodeEpoxyModelWithHolder_()
                    .id(item._id)
                    .episode_id(item._id)
                    .title(item.title?.rendered?.htmlToText())
                    .description(item.excerpt?.rendered?.htmlToText())
                    .date(item.date?.toUTCDate(EPISODE_DATE_FORMAT))
                    .imageUrl(imageHttpsUrl)

                    .upvoted(item.upvoted)
                    .score(item.score)
                    .upvoteClickListener { upvoteClickListener(item._id) }

                    .commentsCount(item.thread?.commentsCount)
                    .commentClickListener { commentClickListener(item._id) }

                    .bookmarked(item.bookmarked)
                    .bookmarkClickListener { bookmarkClickListener(item._id) }

                    .episodeClickListener { episodeClickListener(item._id) }
        }
    }

    private fun String.htmlToText(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(this, 0)
        } else {
            Html.fromHtml(this)
        }.toString()
    }

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

    // FIXME :: Remove
    init {
        isDebugLoggingEnabled = true
    }

    // FIXME :: Timber log
    override fun onExceptionSwallowed(exception: RuntimeException) {
        throw exception
    }

}