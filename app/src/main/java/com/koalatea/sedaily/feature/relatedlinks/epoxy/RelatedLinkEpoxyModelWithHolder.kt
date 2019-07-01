package com.koalatea.sedaily.feature.relatedlinks.epoxy

import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.koalatea.sedaily.R
import com.koalatea.sedaily.ui.epoxy.KotlinEpoxyHolder

@EpoxyModelClass(layout = R.layout.view_holder_related_link)
abstract class RelatedLinkEpoxyModelWithHolder : EpoxyModelWithHolder<RelatedLinkHolder>() {

    @EpoxyAttribute lateinit var title: String
    @EpoxyAttribute lateinit var relatedLinkClickListener: () -> Unit

    @CallSuper
    override fun bind(holder: RelatedLinkHolder) {
        holder.titleTextView.text = title

        holder.containerConstraintLayout.setOnClickListener { relatedLinkClickListener() }
    }

}

class RelatedLinkHolder : KotlinEpoxyHolder() {
    val containerConstraintLayout by bind<ConstraintLayout>(R.id.containerConstraintLayout)
    val titleTextView by bind<TextView>(R.id.titleTextView)
}