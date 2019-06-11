package com.koalatea.sedaily.database.converter

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.koalatea.sedaily.database.model.*
import com.google.gson.reflect.TypeToken



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
    fun threadToJsonString(thread: Thread?): String? {
        return thread?.let { gson.toJson(thread) }
    }

    @TypeConverter
    fun jsonStringToThread(string: String?): Thread? {
        return string?.let { gson.fromJson(string, Thread::class.java) }
    }

    @TypeConverter
    fun filteredTagsToJsonString(tags: List<Tag>?): String? {
        return tags?.let { gson.toJson(tags) }
    }

    @TypeConverter
    fun jsonStringToFilteredTags(string: String?): List<Tag>? {
        return string?.let { gson.fromJson(string, object : TypeToken<ArrayList<Tag>>() {}.type) }
    }

}