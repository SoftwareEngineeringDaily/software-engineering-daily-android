package com.koalatea.sedaily.database.converter

import android.net.Uri
import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.koalatea.sedaily.database.model.*

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
    fun jsonStringToThread(json: String?): Thread? {
        return json?.let { gson.fromJson(json, Thread::class.java) }
    }

    @TypeConverter
    fun filteredTagsToJsonString(tags: List<Tag>?): String? {
        return tags?.let { gson.toJson(tags) }
    }

    @TypeConverter
    fun jsonStringToFilteredTags(json: String?): List<Tag>? {
        return json?.let { gson.fromJson(json, object : TypeToken<ArrayList<Tag>>() {}.type) }
    }

    @TypeConverter
    fun uriFromString(value: String?): Uri? {
        return value?.let { Uri.parse(value) }
    }

    @TypeConverter
    fun uriToString(uri: Uri?): String? {
        return uri.toString()
    }

}