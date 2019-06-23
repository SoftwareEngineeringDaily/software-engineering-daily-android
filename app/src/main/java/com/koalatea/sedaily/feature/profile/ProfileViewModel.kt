package com.koalatea.sedaily.feature.profile

import androidx.lifecycle.*
import com.koalatea.sedaily.database.model.Episode
import com.koalatea.sedaily.model.Profile
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.network.response.AuthResponse
import com.koalatea.sedaily.repository.SessionRepository
import com.koalatea.sedaily.repository.UserRepository
import com.koalatea.sedaily.util.Event

class ProfileViewModel internal constructor(
        private val userRepository: UserRepository,
        private val sessionRepository: SessionRepository
) : ViewModel() {

    private val profileLiveData = MutableLiveData<Unit>()
    val profileResource: LiveData<Resource<Profile>> = Transformations.switchMap(profileLiveData) {
        liveData {
            emit(Resource.Loading)

            val resource = userRepository.fetchProfile()
            emit(resource)
        }
    }

    private val _navigateToLogin = MutableLiveData<Event<Boolean>>()
    val navigateToLogin: LiveData<Event<Boolean>>
        get() = _navigateToLogin

    fun fetchProfile() {
        if (sessionRepository.isLoggedIn) {
            if (profileLiveData.value == null) {
                profileLiveData.value = Unit
            }
        } else {
            _navigateToLogin.value = Event(true)
        }
    }

}