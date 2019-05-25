package com.koalatea.sedaily.feature.home

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject


class PodcastSearchRepo private constructor() {
    private val searchChange = PublishSubject.create<String>()
    var currentSearch = ""

    val getSearchChange: Observable<String>
        get() = searchChange

    fun setSearch(newSearch: String) {
        currentSearch = newSearch
        searchChange.onNext(newSearch)
    }

    companion object {
        private var instance: PodcastSearchRepo? = null

        fun getInstance(): PodcastSearchRepo {
            if (instance == null) {
                instance = PodcastSearchRepo()
            }
            return instance as PodcastSearchRepo
        }
    }
}