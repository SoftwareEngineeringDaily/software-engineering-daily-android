package com.koalatea.sedaily.ui.epoxy

import android.text.format.DateFormat
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.button.MaterialButton
import com.koalatea.sedaily.R
import java.util.*

abstract class BaseEpisodeEpoxyModelWithHolder<Holder : BaseEpisodeHolder> : EpoxyModelWithHolder<Holder>() {

    @EpoxyAttribute var title: String? = null
    @EpoxyAttribute var description: String? = null
    @EpoxyAttribute var date: Date? = null
    @EpoxyAttribute var imageUrl: String? = null

    @EpoxyAttribute var upvoted: Boolean? = null
    @EpoxyAttribute var score: Int? = null
    @EpoxyAttribute lateinit var upvoteClickListener: () -> Unit

    @EpoxyAttribute var commentsCount: Int? = null
    @EpoxyAttribute lateinit var commentClickListener: () -> Unit

    @EpoxyAttribute var bookmarked: Boolean? = null
    @EpoxyAttribute lateinit var bookmarkClickListener: () -> Unit

    @EpoxyAttribute lateinit var episodeClickListener: () -> Unit

    @CallSuper
    override fun bind(holder: Holder) {
        renderDetails(holder)
        renderActions(holder)

        holder.containerConstraintLayout.setOnClickListener { episodeClickListener() }
    }

    @CallSuper
    protected open fun renderDetails(holder: Holder) {
        val context = holder.episodeImageView.context

        val imageCornerRadius = context.resources.getDimension(R.dimen.image_corner_radius).toInt()
        Glide.with(context)
                .load(imageUrl)
                .transform(MultiTransformation(CenterCrop(), RoundedCorners(imageCornerRadius)))
                .placeholder(R.drawable.vd_image)
                .error(R.drawable.vd_broken_image)
                .into(holder.episodeImageView)

        holder.titleTextView.text = title
        date?.let {
            holder.dateTextView.text = DateFormat.getDateFormat(context).format(date)
            holder.dateTextView.visibility = VISIBLE
        } ?: run {
            holder.dateTextView.visibility = GONE
        }

        holder.descriptionTextView.text = description
    }

    @CallSuper
    protected open fun renderActions(holder: Holder) {
        holder.likesButton.setIconResource(if (upvoted == true) R.drawable.vd_favorite else R.drawable.vd_favorite_border)
        holder.likesButton.text = score?.let {
            if (it > 0) { it.toString() } else { "" }
        }
        holder.likesButton.setOnClickListener { upvoteClickListener() }

        holder.commentsButton.text = commentsCount?.let {
            if (it > 0) { it.toString() } else { "" }
        }
        holder.commentsButton.setOnClickListener { commentClickListener() }

        holder.bookmarkButton.setIconResource(if (bookmarked == true) R.drawable.vd_bookmark else R.drawable.vd_bookmark_border)
        holder.bookmarkButton.setOnClickListener { bookmarkClickListener() }
    }

}

abstract class BaseEpisodeHolder : KotlinEpoxyHolder() {
    val containerConstraintLayout by bind<ConstraintLayout>(R.id.containerConstraintLayout)
    val episodeImageView by bind<ImageView>(R.id.guestImageView)
    val titleTextView by bind<TextView>(R.id.titleTextView)
    val dateTextView by bind<TextView>(R.id.dateTextView)
    val descriptionTextView by bind<TextView>(R.id.descriptionTextView)
    val likesButton by bind<MaterialButton>(R.id.likesButton)
    val commentsButton by bind<Button>(R.id.commentsButton)
    val bookmarkButton by bind<MaterialButton>(R.id.bookmarkButton)
}