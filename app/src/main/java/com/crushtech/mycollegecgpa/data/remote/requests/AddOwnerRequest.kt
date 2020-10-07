package com.crushtech.mycollegecgpa.data.remote.requests

data class AddOwnerRequest(
    val SemesterId: String,
    val owner: String
)