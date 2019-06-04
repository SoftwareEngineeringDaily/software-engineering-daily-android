package com.koalatea.sedaily.model

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder

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