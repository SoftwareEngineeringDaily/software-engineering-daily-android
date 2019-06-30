package com.koalatea.sedaily.feature.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koalatea.sedaily.feature.auth.event.ValidationStatus
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.network.response.AuthResponse
import com.koalatea.sedaily.repository.SessionRepository
import com.koalatea.sedaily.repository.UserRepository
import com.koalatea.sedaily.util.Event
import kotlinx.coroutines.launch

class AuthViewModel(
        private val userRepository: UserRepository,
        private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _userLiveData = MutableLiveData<Event<Resource<AuthResponse>>>()
    val userLiveData: LiveData<Event<Resource<AuthResponse>>>
        get() = _userLiveData

    private val _validationLiveData = MutableLiveData<Event<ValidationStatus>>()
    val validationLiveData: LiveData<Event<ValidationStatus>>
        get() = _validationLiveData

    fun login(username: String, password: String) = viewModelScope.launch {
        if (validate(username, password = password)) {
            _userLiveData.postValue(Event(Resource.Loading))

            val resource = userRepository.login(username, password)
            handleUserResource(resource)

            _userLiveData.postValue(Event(resource))
        }
    }

    fun register(username: String, email: String, password: String) = viewModelScope.launch {
        if (validate(username, email, password)) {
            _userLiveData.postValue(Event(Resource.Loading))

            val resource = userRepository.register(username, email, password)
            handleUserResource(resource)

            _userLiveData.postValue(Event(resource))
        }
    }

    private fun validate(username: String, email: String? = null, password: String): Boolean {
        val isUsernameValid = username.isNotBlank()
        val isEmailValid = email?.let {
            email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        } ?: true
        val isPasswordValid = password.isNotBlank()

        _validationLiveData.postValue(Event(ValidationStatus(isUsernameValid, isEmailValid, isPasswordValid)))

        return isUsernameValid && isEmailValid && isPasswordValid
    }

    private fun handleUserResource(resource: Resource<AuthResponse>) {
        if (resource is Resource.Success) {
            sessionRepository.token = resource.data.token
        }
    }

}