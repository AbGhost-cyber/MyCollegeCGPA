package com.crushtech.myccgpa.data.remote.requests

data class SignInRequest(
    val email: String,
    val password: String,
    val username: String = "user1"
)