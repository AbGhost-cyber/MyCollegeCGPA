package com.crushtech.mycollegecgpa.data.remote.requests

data class ThirdPartyAuthRequest(
    val email: String,
    val username: String = "user"
)