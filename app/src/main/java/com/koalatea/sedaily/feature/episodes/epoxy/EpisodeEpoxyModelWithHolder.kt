package com.koalatea.sedaily.feature.episodes.epoxy

import android.view.View
import android.widget.ProgressBar
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.koalatea.sedaily.R
import com.koalatea.sedaily.ui.epoxy.BaseEpisodeEpoxyModelWithHolder
import com.koalatea.sedaily.ui.epoxy.BaseEpisodeHolder

@EpoxyModelClass(layout = R.layout.view_holder_episode)
abstract class EpisodeEpoxyModelWithHolder : BaseEpisodeEpoxyModelWithHolder<EpisodeHolder>() {

    @EpoxyAttribute var listenProgress: Int? = null

    override fun renderDetails(holder: EpisodeHolder) {
        super.renderDetails(holder)

        listenProgress?.let {
            holder.listenedProgressBar.apply {
                progress = it
                visibility = View.VISIBLE
            }
        } ?: run {
            holder.listenedProgressBar.visibility = View.GONE
        }
    }

}

class EpisodeHolder : BaseEpisodeHolder() {
    val listenedProgressBar by bind<ProgressBar>(R.id.listenedProgressBar)
}