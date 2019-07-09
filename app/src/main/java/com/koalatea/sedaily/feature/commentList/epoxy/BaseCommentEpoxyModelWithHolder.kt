package com.koalatea.sedaily.feature.commentList.epoxy

import android.text.format.DateFormat
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.CallSuper
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.koalatea.sedaily.R
import com.koalatea.sedaily.ui.epoxy.KotlinEpoxyHolder
import java.util.*

abstract class BaseCommentEpoxyModelWithHolder<Holder: BaseCommentHolder> : EpoxyModelWithHolder<Holder>() {

    @EpoxyAttribute var authorImageUrl: String? = null
    @EpoxyAttribute lateinit var authorName: String
    @EpoxyAttribute lateinit var comment: String
    @EpoxyAttribute var date: Date? = null

    @CallSuper
    override fun bind(holder: Holder) {
        val context = holder.authorImageView.context

        Glide.with(context)
                .load(authorImageUrl)
                .transform(MultiTransformation(CenterCrop(), CircleCrop()))
                .placeholder(R.drawable.vd_image)
                .error(R.drawable.vd_broken_image)
                .into(holder.authorImageView)

        holder.authorNameTextView.text = authorName
        holder.commentTextView.text = comment

        date?.let {
            holder.dateTextView.text = DateFormat.getDateFormat(context).format(date)
            holder.dateTextView.visibility = View.VISIBLE
        } ?: run {
            holder.dateTextView.visibility = View.GONE
        }
    }

}

abstract class BaseCommentHolder : KotlinEpoxyHolder() {
    val authorImageView by bind<ImageView>(R.id.authorImageView)
    val authorNameTextView by bind<TextView>(R.id.authorNameTextView)
    val commentTextView by bind<TextView>(R.id.commentTextView)
    val dateTextView by bind<TextView>(R.id.dateTextView)
}