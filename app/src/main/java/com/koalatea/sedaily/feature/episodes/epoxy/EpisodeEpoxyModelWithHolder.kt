package com.koalatea.sedaily.feature.episodes.epoxy

import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.Glide
import com.koalatea.sedaily.R
import com.koalatea.sedaily.util.KotlinEpoxyHolder

@EpoxyModelClass(layout = R.layout.view_holder_episode)
abstract class EpisodeEpoxyModelWithHolder : EpoxyModelWithHolder<Holder>() {

    @EpoxyAttribute
    var title: String? = null
    @EpoxyAttribute
    var description: String? = null
    @EpoxyAttribute
    var date: String? = null
    @EpoxyAttribute
    var imageUrl: String? = null

//    @EpoxyAttribute lateinit var likesClickListener: () -> Unit
//    @EpoxyAttribute lateinit var commentsClickListener: () -> Unit
//    @EpoxyAttribute lateinit var bookmarkClickListener: () -> Unit

    @EpoxyAttribute
    lateinit var episodeClickListener: () -> Unit

    override fun bind(holder: Holder) {
        holder.titleTextView.text = title
        holder.descriptionTextView.text = description
        holder.dateTextView.text = date

        // FIXME :: Add placeholder and error messages + transformation
        Glide.with(holder.episodeImageView.context).load(imageUrl).into(holder.episodeImageView)

        holder.containerConstraintLayout.setOnClickListener { episodeClickListener }
    }

}

class Holder : KotlinEpoxyHolder() {
    val containerConstraintLayout by bind<ConstraintLayout>(R.id.containerConstraintLayout)
    val episodeImageView by bind<ImageView>(R.id.episodeImageView)
    val titleTextView by bind<TextView>(R.id.titleTextView)
    val dateTextView by bind<TextView>(R.id.dateTextView)
    val descriptionTextView by bind<TextView>(R.id.descriptionTextView)
    val likesButton by bind<Button>(R.id.likesButton)
    val commentsButton by bind<Button>(R.id.commentsButton)
    val bookmarkButton by bind<Button>(R.id.bookmarkButton)
    val listenedProgressBar by bind<ProgressBar>(R.id.listenedProgressBar)
}