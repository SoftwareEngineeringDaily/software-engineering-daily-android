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

    override fun buildItemModel(currentPosition: Int, episode: Episode?): EpoxyModel<*> {
        return if (episode == null) {
            EpisodeEpoxyModelWithHolder_()
                    .id(-currentPosition)
                    .title("loading $currentPosition")
        } else {
            EpisodeEpoxyModelWithHolder_()
                    .id(episode._id)
                    .title(episode.titleString)
                    .description(episode.excerptString)
                    .date(episode.utcDate)
                    .imageUrl(episode.httpsFeaturedImageUrl)

                    .upvoted(episode.upvoted)
                    .score(episode.score)
                    .upvoteClickListener { upvoteClickListener(episode) }

                    .commentsCount(episode.thread?.commentsCount)
                    .commentClickListener { commentClickListener(episode) }

                    .bookmarked(episode.bookmarked)
                    .bookmarkClickListener { bookmarkClickListener(episode) }

                    .episodeClickListener { episodeClickListener(episode) }
        }
    }

}