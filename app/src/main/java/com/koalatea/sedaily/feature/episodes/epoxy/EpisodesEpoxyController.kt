package com.koalatea.sedaily.feature.episodes.epoxy

import androidx.recyclerview.widget.DiffUtil
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.koalatea.sedaily.database.model.Episode
import com.koalatea.sedaily.database.model.EpisodeDetails

class EpisodesEpoxyController(
        private val upvoteClickListener: (episode: Episode) -> Unit,
        private val commentClickListener: (episode: Episode) -> Unit,
        private val bookmarkClickListener: (episode: Episode) -> Unit,
        private val episodeClickListener: (episode: Episode) -> Unit
) : PagedListEpoxyController<EpisodeDetails>(
        itemDiffCallback = object : DiffUtil.ItemCallback<EpisodeDetails>() {
            override fun areItemsTheSame(oldItem: EpisodeDetails, newItem: EpisodeDetails): Boolean = oldItem.episode._id == newItem.episode._id

            override fun areContentsTheSame(oldItem: EpisodeDetails, newItem: EpisodeDetails): Boolean = oldItem == newItem
        }
) {

    override fun buildItemModel(currentPosition: Int, item: EpisodeDetails?): EpoxyModel<*> {
        val episode = item?.episode

        return if (episode == null) {
            EpisodeEpoxyModelWithHolder_()
                    .id(-currentPosition)
                    .title("loading $currentPosition")
        } else {
            val progress = item.listened?.let { listened ->
                100.0 * listened.startPosition / listened.total
            }

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

                    .listenProgress(progress?.toInt())

                    .episodeClickListener { episodeClickListener(episode) }
        }
    }

}