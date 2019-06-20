package com.koalatea.sedaily.feature.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koalatea.sedaily.model.User
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.repository.SessionRepository
import com.koalatea.sedaily.repository.UserRepository
import com.koalatea.sedaily.util.Event
import kotlinx.coroutines.launch

class AuthViewModel(
        private val userRepository: UserRepository,
        private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _userLiveData = MutableLiveData<Event<Resource<User>>>()
    val userLiveData: LiveData<Event<Resource<User>>>
        get() = _userLiveData

    fun login(username: String, password: String) = viewModelScope.launch {
        // FIXME :: Validation before all that

        _userLiveData.postValue(Event(Resource.Loading))

        val resource = userRepository.login(username, password)
        handleUserResource(resource)

        _userLiveData.postValue(Event(resource))
    }

    fun register(username: String, email: String, password: String) = viewModelScope.launch {
        // FIXME :: Validation before all that

        _userLiveData.postValue(Event(Resource.Loading))

        val resource = userRepository.register(username, email, password)
        handleUserResource(resource)

        _userLiveData.postValue(Event(resource))
    }

    private fun handleUserResource(resource: Resource<User>) {
        if (resource is Resource.Success) {
            sessionRepository.token = resource.data.token
        }
    }

}