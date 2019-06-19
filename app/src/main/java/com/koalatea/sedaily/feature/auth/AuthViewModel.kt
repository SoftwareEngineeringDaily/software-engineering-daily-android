package com.koalatea.sedaily.feature.auth

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koalatea.sedaily.model.User
import com.koalatea.sedaily.network.SEDailyApi
import com.koalatea.sedaily.network.toException
import com.koalatea.sedaily.repository.UserRepository
import com.koalatea.sedaily.util.safeApiCall
import kotlinx.coroutines.launch

class AuthViewModel(
        private val userRepository: UserRepository
) : ViewModel() {

//    private var screen: String = "Register"
//    private val showRegister = MutableLiveData<Int>()
//    private val submissionText = MutableLiveData<String>()
//    private val toggleText = MutableLiveData<String>()
//    private val usernameText = MutableLiveData<String>()
//
//    val userToken = MutableLiveData<String>()
//    val authError = MutableLiveData<Throwable>()
//
//    init {
//        setUpRegister()
//    }
//
//    private fun setUpRegister() {
//        showRegister.value = View.VISIBLE
//        submissionText.value = "Register"
//        toggleText.value = "Login"
//        usernameText.value = "Username"
//    }
//
//    private fun setUpLogin() {
//        showRegister.value = View.GONE
//        submissionText.value = "Login"
//        toggleText.value = "Register"
//        usernameText.value = "Username or Email"
//    }
//
//    fun getShowRegister(): MutableLiveData<Int> {
//        return showRegister
//    }
//
//    fun getSubmissionText(): MutableLiveData<String> {
//        return submissionText
//    }
//
//    fun getToggleText(): MutableLiveData<String> {
//        return toggleText
//    }
//
//    fun getUsernameText(): MutableLiveData<String> {
//        return usernameText
//    }
//
//    fun toggleScreen() {
//        if (screen == "Register") {
//            screen = "Login"
//            setUpLogin()
//            return
//        }
//
//        screen = "Register"
//        setUpRegister()
//    }
//
//    fun authenticate(username: String, password: String, email: String) {
////        logLoginRegAnalytics(username, type)
//
//        var emailToSubmit = ""
//        // Check if user is using email for login
//        if (email.isEmpty() && username.contains("@")) {
//            emailToSubmit = username
//        }
//
//        viewModelScope.launch {
//            val response = safeApiCall {
//                if (screen === "Register") {
//                    sedailyApi.registerAsync(username, email, password)
//                } else {
//                    sedailyApi.loginAsync(username, email, password)
//                }.await()
//            }
//
//            val user = response?.body()
//            if (response?.isSuccessful == true && user != null) {
//                handleLoginSuccess(user)
//            } else {
//                handleLoginError(response?.errorBody().toException())
//            }
//        }
//    }
//
//    private fun handleLoginSuccess(user: User) {
//        userRepository.token = user.token
//        userToken.value = user.token
//    }
//
//    private fun handleLoginError(error: Throwable) {
//        authError.value = error
//    }

}