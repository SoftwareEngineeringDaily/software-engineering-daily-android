package com.koalatea.sedaily.feature.episodes.epoxy

import androidx.recyclerview.widget.DiffUtil
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.koalatea.sedaily.database.model.Episode

class EpisodesEpoxyController(
        private val upvoteClickListener: (episode: Episode) -> Unit,
        private val commentClickListener: (episode: Episode) -> Unit,
        private val bookmarkClickListener: (episode: Episode) -> Unit,
        private val episodeClickListener: (episode: Episode) -> Unit
) : PagedListEpoxyController<Episode>(
        itemDiffCallback = object : DiffUtil.ItemCallback<Episode>() {
            override fun areItemsTheSame(oldItem: Episode, newItem: Episode): Boolean = oldItem._id == newItem._id

            override fun areContentsTheSame(oldItem: Episode, newItem: Episode): Boolean = oldItem == newItem
        }
) {

    override fun buildItemModel(currentPosition: Int, item: Episode?): EpoxyModel<*> {
        return if (item == null) {
            EpisodeEpoxyModelWithHolder_()
                    .id(-currentPosition)
                    .title("loading $currentPosition")
        } else {
            EpisodeEpoxyModelWithHolder_()
                    .id(item._id)
                    .title(item.titleString)
                    .description(item.excerptString)
                    .date(item.utcDate)
                    .imageUrl(item.httpsFeaturedImageUrl)

                    .upvoted(item.upvoted)
                    .score(item.score)
                    .upvoteClickListener { upvoteClickListener(item) }

                    .commentsCount(item.thread?.commentsCount)
                    .commentClickListener { commentClickListener(item) }

                    .bookmarked(item.bookmarked)
                    .bookmarkClickListener { bookmarkClickListener(item) }

                    .episodeClickListener { episodeClickListener(item) }
        }
    }

}