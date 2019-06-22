package com.koalatea.sedaily.feature.bookmarks

import androidx.lifecycle.ViewModel
import com.koalatea.sedaily.repository.SessionRepository

class BookmarksViewModel internal constructor(
        private val sessionRepository: SessionRepository
) : ViewModel() {

}