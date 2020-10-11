package com.crushtech.mycollegecgpa.data.remote.requests

data class AddOwnerRequest(
    val owner: String,
    val semesterId: String
)