package com.koalatea.sedaily.auth

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.koalatea.sedaily.models.User
import com.koalatea.sedaily.network.NetworkHelper
import com.koalatea.sedaily.network.SEDailyApi
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class AuthViewModel : ViewModel() {
    var sedailyApi: SEDailyApi = NetworkHelper.getApi()
    private var screen: String = "Register"
    private val showRegister = MutableLiveData<Int>()
    private val submissionText = MutableLiveData<String>()
    private val toggleText = MutableLiveData<String>()
    private val usernameText = MutableLiveData<String>()
    private val compositeDisposable = CompositeDisposable()

    val userToken = MutableLiveData<String>()
    val authError = MutableLiveData<Throwable>()

    init {
        setUpRegister()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    private fun setUpRegister() {
        showRegister.value = View.VISIBLE
        submissionText.value = "Register"
        toggleText.value = "Login"
        usernameText.value = "Username"
    }

    private fun setUpLogin() {
        showRegister.value = View.GONE
        submissionText.value = "Login"
        toggleText.value = "Register"
        usernameText.value = "Username or Email"
    }

    fun getShowRegister(): MutableLiveData<Int> {
        return showRegister
    }

    fun getSubmissionText(): MutableLiveData<String> {
        return submissionText
    }

    fun getToggleText(): MutableLiveData<String> {
        return toggleText
    }

    fun getUsernameText(): MutableLiveData<String> {
        return usernameText
    }

    fun toggleScreen() {
        if (screen == "Register") {
            screen = "Login"
            setUpLogin()
            return
        }

        screen = "Register"
        setUpRegister()
    }

    fun authenticate(username: String, password: String, email: String) {
//        logLoginRegAnalytics(username, type)

        var emailToSubmit = ""
        // Check if user is using email for login
        if (email.isEmpty() && username.contains("@")) {
            emailToSubmit = username
        }

        val disposable = getQuery(username, password, emailToSubmit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.handleLoginSuccess(it) }, { this.handleLoginError(it) })
        compositeDisposable.add(disposable)
    }

    private fun handleLoginSuccess(user: User) {
        UserRepository.getInstance().setToken(user.token!!)
        userToken.value = user.token
    }

    private fun handleLoginError(error: Throwable) {
        authError.value = error
    }

    private fun getQuery(username: String,  password: String, email: String): Single<User> {
        val mService = sedailyApi
        return if (screen === "Register") {
            mService.register(username, email, password)
        } else mService.login(username, email, password)

    }
}