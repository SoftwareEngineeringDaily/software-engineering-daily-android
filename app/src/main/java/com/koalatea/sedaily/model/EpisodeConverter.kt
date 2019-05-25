package com.koalatea.sedaily.model

import androidx.room.TypeConverter

class EpisodeConverter {
    @TypeConverter
    fun titleToString(title: Title): String {
        return title.rendered
    }

    @TypeConverter
    fun stringToTitle(string: String): Title {
        val title = Title(string)
        return title
    }

    @TypeConverter
    fun contentToString(content: Content): String {
        return content.rendered
    }

    @TypeConverter
    fun stringToContent(string: String): Content {
        val content = Content(string)
        return content
    }

    @TypeConverter
    fun excerptToString(content: Excerpt): String {
        return content.rendered
    }

    @TypeConverter
    fun stringToExcerpt(string: String): Excerpt {
        val content = Excerpt(string)
        return content
    }
}