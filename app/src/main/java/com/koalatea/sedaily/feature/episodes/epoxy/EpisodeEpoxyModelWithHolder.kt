package com.koalatea.sedaily.feature.episodes.epoxy

import android.widget.ProgressBar
import com.airbnb.epoxy.EpoxyModelClass
import com.koalatea.sedaily.R
import com.koalatea.sedaily.ui.epoxy.BaseEpisodeEpoxyModelWithHolder
import com.koalatea.sedaily.ui.epoxy.BaseEpisodeHolder

@EpoxyModelClass(layout = R.layout.view_holder_episode)
abstract class EpisodeEpoxyModelWithHolder : BaseEpisodeEpoxyModelWithHolder<EpisodeHolder>()

class EpisodeHolder : BaseEpisodeHolder() {
    val listenedProgressBar by bind<ProgressBar>(R.id.listenedProgressBar)
}