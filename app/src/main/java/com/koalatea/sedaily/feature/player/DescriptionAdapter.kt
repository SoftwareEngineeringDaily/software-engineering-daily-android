package com.koalatea.sedaily.feature.player

import android.app.PendingIntent
import android.graphics.Bitmap
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class DescriptionAdapter : PlayerNotificationManager.MediaDescriptionAdapter {

    override fun createCurrentContentIntent(player: Player?): PendingIntent? {
        return null
    }

    override fun getCurrentLargeIcon(player: Player?, callback: PlayerNotificationManager.BitmapCallback?): Bitmap? {
        return null
    }

    override fun getCurrentContentTitle(player: Player): String {
        val window = player.currentWindowIndex
        return "Title"//getTitle(window);
    }

    override fun getCurrentContentText(player: Player): String? {
        val window = player.currentWindowIndex
        return "Description"//getDescription(window);
    }
}
