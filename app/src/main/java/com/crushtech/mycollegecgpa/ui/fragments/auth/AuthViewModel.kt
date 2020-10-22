package com.crushtech.mycollegecgpa.ui.fragments.auth

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crushtech.mycollegecgpa.repositories.SemesterRepository
import com.crushtech.mycollegecgpa.utils.Resource
import kotlinx.coroutines.launch
import java.util.regex.Matcher
import java.util.regex.Pattern

class AuthViewModel @ViewModelInject constructor(
    private val repository: SemesterRepository
) : ViewModel() {
    private val _registerStatus = MutableLiveData<Resource<String>>()
    val registerStatus: LiveData<Resource<String>> = _registerStatus

    private val _loginStatus = MutableLiveData<Resource<String>>()
    val loginStatus: LiveData<Resource<String>> = _loginStatus

    fun register(email: String, password: String, repeatedPassword: String, username: String) {
        //emit loading state, notify the observers that we are starting the register function
        _registerStatus.postValue(Resource.loading(null))
        if (email.isEmpty() || password.isEmpty() || repeatedPassword.isEmpty() || username.isEmpty()) {
            _registerStatus.postValue(
                Resource.error(
                    "Please fill out the required fields",
                    null
                )
            )
            return
        }
        val pattern: Pattern = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(password)
        val containsSpecialChar: Boolean = matcher.find()
        if (!containsSpecialChar) {
            _registerStatus.postValue(
                Resource.error(
                    "your password must consist of at least one special character",
                    null
                )
            )
            return
        }
        if (password.length < 6) {
            _registerStatus.postValue(
                Resource.error(
                    "your password must be up to 6 letters",
                    null
                )
            )
            return
        }
        if (password != repeatedPassword) {
            _registerStatus.postValue(
                Resource.error(
                    "Passwords do not match",
                    null
                )
            )
            return
        }
        viewModelScope.launch {
            val result = repository.register(email, password, username)
            _registerStatus.postValue(result)
        }
    }


    fun login(email: String, password: String) {
        //emit loading state, notify the observers that we are starting the login function
        _loginStatus.postValue(Resource.loading(null))
        if (email.isEmpty() || password.isEmpty()) {
            _loginStatus.postValue(
                Resource.error(
                    "Please fill out the required fields",
                    null
                )
            )
            return
        }
        viewModelScope.launch {
            val result = repository.login(email, password)
            _loginStatus.postValue(result)
        }
    }

}