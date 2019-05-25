package com.koalatea.sedaily.feature.auth

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AlertDialog

object AlertUtil {
    fun displayMessage(context: Context, message: String) {
        val builder: AlertDialog.Builder

        // Ensure context is active
        if (context is Activity) {
            if (context.isFinishing) {
                return
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert)
        } else {
            builder = AlertDialog.Builder(context)
        }

        builder.setTitle("Error")
                .setMessage(message)
                .setPositiveButton(android.R.string.yes) { _, _ -> }
                .setNegativeButton(android.R.string.no) { _, _ -> }
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
    }
}