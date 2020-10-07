package com.crushtech.mycollegecgpa.data.remote.requests

data class SignInRequest(
    val email: String,
    val password: String,
    val username: String
)