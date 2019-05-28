package com.koalatea.sedaily.feature.home

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class PodcastSearchRepository {

    private val searchChange = PublishSubject.create<String>()
    var currentSearch = ""

    val getSearchChange: Observable<String>
        get() = searchChange

    fun setSearch(newSearch: String) {
        currentSearch = newSearch
        searchChange.onNext(newSearch)
    }

}