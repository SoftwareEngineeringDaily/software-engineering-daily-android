package com.koalatea.sedaily.feature.commentList.epoxy

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyViewHolder
import timber.log.Timber
import kotlin.math.roundToInt

class CommentsItemDecoration(
        context: Context,
        private val offsetStart: Int
) : RecyclerView.ItemDecoration() {

    private val attributesArray = intArrayOf(android.R.attr.listDivider)

    private var dividerDrawable: Drawable? = null

    private val boundsRect = Rect()

    init {
        val typedArray = context.obtainStyledAttributes(attributesArray)
        dividerDrawable = typedArray.getDrawable(0)
        if (dividerDrawable == null) {
            Timber.w("@android:attr/listDivider was not set in the theme used for this " + "DividerItemDecoration. Please set that attribute all call setDrawable()")
        }
        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.layoutManager == null || dividerDrawable == null) {
            return
        }

        canvas.save()

        val left = offsetStart
        val top = 0
        val right = parent.width

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val viewHolder = parent.getChildViewHolder(child)
            if ((viewHolder as EpoxyViewHolder).model is ReplyEpoxyModelWithHolder) {
                parent.layoutManager?.getDecoratedBoundsWithMargins(child, boundsRect)
                val bottom = boundsRect.bottom + child.translationY.roundToInt()
                dividerDrawable?.setBounds(left, top, right, bottom)
                dividerDrawable?.draw(canvas)
            }
        }

        canvas.restore()
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (dividerDrawable == null) {
            outRect.set(0, 0, 0, 0)
            return
        }

        val viewHolder = parent.getChildViewHolder(view)
        if ((viewHolder as EpoxyViewHolder).model is ReplyEpoxyModelWithHolder) {
            outRect.set(0, 0, 0, dividerDrawable?.intrinsicHeight ?: 0)
        } else {
            outRect.setEmpty()
        }
    }

}