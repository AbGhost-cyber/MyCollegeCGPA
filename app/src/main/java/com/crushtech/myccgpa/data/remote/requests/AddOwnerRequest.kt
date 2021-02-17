package com.crushtech.myccgpa.data.remote.requests

data class AddOwnerRequest(
    val owner: String,
    val semesterId: String
)