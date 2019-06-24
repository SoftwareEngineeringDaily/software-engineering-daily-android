package com.koalatea.sedaily.network

import com.koalatea.sedaily.model.Profile

sealed class ProfileResult {
    data class LoggedIn(val profile: Profile) : ProfileResult()
    object LoggedOut : ProfileResult()
}