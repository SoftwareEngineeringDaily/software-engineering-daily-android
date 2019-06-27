package com.koalatea.sedaily.feature.commentList.epoxy

import com.airbnb.epoxy.EpoxyModelClass
import com.koalatea.sedaily.R

@EpoxyModelClass(layout = R.layout.view_holder_reply)
abstract class ReplyEpoxyModelWithHolder : BaseCommentEpoxyModelWithHolder<ReplyHolder>()

class ReplyHolder : BaseCommentHolder()