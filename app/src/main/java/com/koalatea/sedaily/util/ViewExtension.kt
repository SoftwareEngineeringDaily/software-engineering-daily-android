package com.koalatea.sedaily.util

import android.content.ContextWrapper
import android.view.View
import androidx.appcompat.app.AppCompatActivity

@Deprecated("")
fun View.getParentActivity(): AppCompatActivity? {
    var context = this.context
    while (context is ContextWrapper) {
        if (context is AppCompatActivity) {
            return context
        }
        context = context.baseContext
    }
    return null
}