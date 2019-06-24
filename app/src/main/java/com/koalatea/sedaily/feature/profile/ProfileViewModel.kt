package com.koalatea.sedaily.feature.profile

import androidx.lifecycle.*
import com.koalatea.sedaily.network.ProfileResult
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.repository.SessionRepository
import com.koalatea.sedaily.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel internal constructor(
        private val userRepository: UserRepository,
        private val sessionRepository: SessionRepository
) : ViewModel() {

    val isUserLoggedIn: Boolean
        get() = sessionRepository.isLoggedIn

    private val profileLiveData = MutableLiveData<Unit>()
    val profileResource: LiveData<Resource<ProfileResult>> = Transformations.switchMap(profileLiveData) {
        liveData {
            emit(Resource.Loading)

            emit(userRepository.fetchProfile())
        }
    }

    fun fetchProfile() {
        profileLiveData.value = Unit
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()

            profileLiveData.value = null
        }
    }

}