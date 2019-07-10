package com.koalatea.sedaily.util

import android.graphics.Rect
import android.view.View

fun View.pointInView(pointX: Float, pointY: Float): Boolean {
    val outRect = Rect()
    val location = IntArray(2)

    getDrawingRect(outRect)
    getLocationOnScreen(location)
    outRect.offset(location[0], location[1])

    return outRect.contains(pointX.toInt(), pointY.toInt())
}
