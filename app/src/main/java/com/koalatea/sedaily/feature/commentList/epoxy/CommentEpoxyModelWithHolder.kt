package com.koalatea.sedaily.feature.commentList.epoxy

import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.koalatea.sedaily.R
import com.koalatea.sedaily.util.KotlinEpoxyHolder

@EpoxyModelClass(layout = R.layout.view_holder_comment)
abstract class CommentEpoxyModelWithHolder : EpoxyModelWithHolder<Holder>() {

    @EpoxyAttribute var authorImageUrl: String? = null
    @EpoxyAttribute lateinit var authorName: String
    @EpoxyAttribute lateinit var comment: String
    @EpoxyAttribute var date: String? = null

    override fun bind(holder: Holder) {
        val context = holder.authorImageView.context

        val imageCornerRadius = context.resources.getDimension(R.dimen.image_corner_radius).toInt()
        Glide.with(context)
                .load(authorImageUrl)
                .transform(MultiTransformation(CenterCrop(), RoundedCorners(imageCornerRadius)))
                .placeholder(R.drawable.vd_image)
                .error(R.drawable.vd_broken_image)
                .into(holder.authorImageView)

        holder.authorNameTextView.text = authorName
        holder.commentTextView.text = comment
        holder.dateTextView.text = date
    }

}

class Holder : KotlinEpoxyHolder() {
    val authorImageView by bind<ImageView>(R.id.authorImageView)
    val authorNameTextView by bind<TextView>(R.id.authorNameTextView)
    val commentTextView by bind<TextView>(R.id.commentTextView)
    val dateTextView by bind<TextView>(R.id.dateTextView)
}