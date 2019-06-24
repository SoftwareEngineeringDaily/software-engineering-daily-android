package com.koalatea.sedaily.feature.bookmarks.epoxy

import com.airbnb.epoxy.EpoxyModelClass
import com.koalatea.sedaily.R
import com.koalatea.sedaily.ui.epoxy.BaseEpisodeEpoxyModelWithHolder
import com.koalatea.sedaily.ui.epoxy.BaseEpisodeHolder

@EpoxyModelClass(layout = R.layout.view_holder_bookmark)
abstract class BookmarkEpoxyModelWithHolder : BaseEpisodeEpoxyModelWithHolder<BookmarkHolder>() {

    override fun renderActions(holder: BookmarkHolder) {
        super.renderActions(holder)

        holder.likesButton.setOnClickListener {
            it.isEnabled = false
            upvoteClickListener()
        }
        holder.likesButton.isEnabled = true

        holder.bookmarkButton.setOnClickListener {
            it.isEnabled = false
            bookmarkClickListener()
        }
        holder.bookmarkButton.isEnabled = true
    }

}

class BookmarkHolder : BaseEpisodeHolder()