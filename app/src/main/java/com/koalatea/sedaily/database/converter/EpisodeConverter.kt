package com.koalatea.sedaily.database.converter

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.koalatea.sedaily.database.table.Content
import com.koalatea.sedaily.database.table.Excerpt
import com.koalatea.sedaily.database.table.Thread
import com.koalatea.sedaily.database.table.Title

class EpisodeConverter {

    private val gson = GsonBuilder().create()

    @TypeConverter
    fun titleToString(title: Title): String {
        return title.rendered
    }

    @TypeConverter
    fun stringToTitle(string: String): Title {
        return Title(string)
    }

    @TypeConverter
    fun contentToString(content: Content): String {
        return content.rendered
    }

    @TypeConverter
    fun stringToContent(string: String): Content {
        return Content(string)
    }

    @TypeConverter
    fun excerptToString(content: Excerpt): String {
        return content.rendered
    }

    @TypeConverter
    fun stringToExcerpt(string: String): Excerpt {
        return Excerpt(string)
    }

    @TypeConverter
    fun threadToJsonString(thread: Thread): String {
        return gson.toJson(thread)
    }

    @TypeConverter
    fun jsonStringToThread(string: String): Thread {
        return gson.fromJson(string, Thread::class.java)
    }
}