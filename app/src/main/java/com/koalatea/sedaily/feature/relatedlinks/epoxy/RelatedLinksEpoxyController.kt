package com.koalatea.sedaily.feature.relatedlinks.epoxy

import android.content.res.Resources
import com.airbnb.epoxy.TypedEpoxyController
import com.koalatea.sedaily.R
import com.koalatea.sedaily.database.model.Episode
import com.koalatea.sedaily.model.RelatedLink

class RelatedLinksEpoxyController(
        private val resources: Resources,
        private val transcriptUrl: String,
        private val viewRelatedLinkClickListener: (url: String) -> Unit
) : TypedEpoxyController<List<RelatedLink>>() {

    override fun buildModels(relatedLinks: List<RelatedLink>) {
        // Add transcript
        relatedLinkEpoxyModelWithHolder {
            id("transcript")
            title(resources.getString(R.string.transcript))

            relatedLinkClickListener { viewRelatedLinkClickListener(transcriptUrl) }
        }

        // Add other links
        relatedLinks.forEach { relatedLink ->
            relatedLinkEpoxyModelWithHolder {
                id(relatedLink._id)
                title(relatedLink.title)

                relatedLinkClickListener { viewRelatedLinkClickListener(relatedLink.url) }
            }
        }
    }

}