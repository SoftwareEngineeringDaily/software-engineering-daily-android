package com.koalatea.sedaily.feature.bookmarks.epoxy

import com.airbnb.epoxy.TypedEpoxyController
import com.koalatea.sedaily.database.model.Episode

class BookmarksEpoxyController(
        private val upvoteClickListener: (episode: Episode) -> Unit,
        private val commentClickListener: (episode: Episode) -> Unit,
        private val bookmarkClickListener: (episode: Episode) -> Unit,
        private val episodeClickListener: (episode: Episode) -> Unit
) : TypedEpoxyController<List<Episode>>() {

    override fun buildModels(episodes: List<Episode>) {
        episodes.forEach { episode ->
            bookmarkEpoxyModelWithHolder {
                id(episode._id)
                title(episode.titleString)
                description(episode.excerptString)
                date(episode.utcDate)
                imageUrl(episode.httpsFeaturedImageUrl)

                upvoted(episode.upvoted)
                score(episode.score)
                upvoteClickListener { upvoteClickListener(episode) }

                commentsCount(episode.thread?.commentsCount)
                commentClickListener { commentClickListener(episode) }

                bookmarked(episode.bookmarked)
                bookmarkClickListener { bookmarkClickListener(episode) }

                episodeClickListener { episodeClickListener(episode) }
            }
        }
    }

}