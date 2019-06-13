package com.koalatea.sedaily.feature.player

import android.content.SharedPreferences

/*
 * @Desc this helps share state between service and front end
 */
class PodcastSessionStateManager constructor(private val sharedPreferences: SharedPreferences) {
//    private val speedChangeObservable = PublishSubject.create<Int>()
//    private val mediaMetaDataChange = PublishSubject.create<MediaMetadataCompat>()
//    private val playBackStateChange = PublishSubject.create<PlaybackStateCompat>()
//
//    private val PROGRESS_KEY = "sedaily-progress-key"
//    private val gson: Gson?
//
//    var currentTitle = ""
//        set(title) {
//            field = title
//            this.previousSave = 0
//        }
//    private var previousSave: Long = 0
//    var currentProgress: Long = 0
    var currentSpeed = 0
        set(currentSpeed) {
            field = currentSpeed
//            speedChangeObservable.onNext(this.currentSpeed)
        }
//    private var mediaMetadataCompat: MediaMetadataCompat? = null
//    private var episodeProgress: MutableMap<String, Long>? = null
//
//    var lastPlaybackState: PlaybackStateCompat? = null
//        set(currentState) {
//            field = currentState
//            playBackStateChange.onNext(this.lastPlaybackState!!) // @TODO: Better
//        }
//
//    val speedChanges: Observable<Int>
//        get() = speedChangeObservable
//
//    val metadataChanges: Observable<MediaMetadataCompat>
//        get() = mediaMetaDataChange
//
//    val playbackStateChanges: Observable<PlaybackStateCompat>
//        get() = playBackStateChange
//
//    init {
//        episodeProgress = HashMap()
//        gson = GsonBuilder().create()
//        val progressString = sharedPreferences.getString(PROGRESS_KEY, "")
//        if (!progressString!!.isEmpty()) {
//            val typeOfHashMap = object : TypeToken<Map<String, Long>>() {}.type
//            episodeProgress = gson.fromJson<Map<String, Long>>(progressString, typeOfHashMap) as MutableMap<String, Long>?
//        }
//    }
//
//    fun getMediaMetaData(): MediaMetadataCompat? {
//        return mediaMetadataCompat
//    }
//
//    fun setMediaMetaData(mediaMetaData: MediaMetadataCompat) {
//        this.mediaMetadataCompat = mediaMetaData
//        mediaMetaDataChange.onNext(mediaMetaData)
//    }
//
//    private fun setProgressForEpisode(_id: String, currentProgress: Long) {
//        if (currentProgress == 0L) return
//
//        this.episodeProgress?.set(_id, currentProgress)
//
//        // Save every 10 seconds
//        val progress = currentProgress / 1000
//        if (previousSave == 0L) {
//            previousSave = progress
//        }
//
//        if (progress - previousSave == 10L && gson != null) {
//            previousSave = progress
//            val json = gson.toJson(this.episodeProgress)
//            val editor = sharedPreferences.edit()
//            editor.putString(PROGRESS_KEY, json)
//            editor.apply()
//        }
//
//    }
//
//    fun getProgressForEpisode(_id: String): Long {
//        // @TODO: Null checks
//        return this.episodeProgress?.get(_id) ?: 0
//    }
//
//    fun saveEpisodeProgress(currentPosition: Long) {
//        val postTile = currentTitle
//        if (postTile.isEmpty()) return
//        setProgressForEpisode(postTile, currentPosition)
//    }
//
//    companion object {
//        private var instance: PodcastSessionStateManager? = null
//
//        @Deprecated("Use Koin instance instead")
//        fun getInstance(): PodcastSessionStateManager {
//            if (instance == null) {
//                instance = PodcastSessionStateManager(
//                        PreferenceManager.getDefaultSharedPreferences(SEDApplication.appContext)
//                )
//            }
//            return instance as PodcastSessionStateManager
//        }
//    }
}