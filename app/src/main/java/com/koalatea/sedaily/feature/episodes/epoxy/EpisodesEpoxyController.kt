package com.koalatea.sedaily.feature.episodes.epoxy

import androidx.navigation.findNavController
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.koalatea.sedaily.feature.home.HomeFragmentDirections
import com.koalatea.sedaily.model.Episode

class EpisodesEpoxyController : PagedListEpoxyController<Episode>() {

    override fun buildItemModel(currentPosition: Int, item: Episode?): EpoxyModel<*> {
        return if (item == null) {
            EpisodeEpoxyModelWithHolder_()
                    .id(-currentPosition)
                    .title("loading $currentPosition")
        } else {
            val imageHttpsUrl = item.featuredImage?.replace(Regex("^http://"), "https://")

            EpisodeEpoxyModelWithHolder_()
                    .id(item._id)
                    .title(item.title?.rendered)
                    .description(item.excerpt?.rendered)
                    .date(item.date)
                    .imageUrl(imageHttpsUrl)
                    .episodeClickListener {
                        // TODO :: Navigate to episode details
//                        val direction = HomeFragmentDirections.openEpisodeDetailsAction(item._id)
//                        it.findNavController().navigate(direction)
                    }
        }
    }

    // FIXME :: Timber log
//    override fun onExceptionSwallowed(exception: RuntimeException) {
//        throw exception
//    }

//    override fun buildModels(photos: List<Photo>, loadingMore: Boolean) {
//        header {
//            id("header")
//            title("My Photos")
//            description("My album description!")
//        }
//
//        photos.forEach {
//            photoView {
//                id(it.id())
//                url(it.url())
//            }
//        }
//
//        if (loadingMore) loaderView { id("loader") }
//    }
}