package com.crushtech.myccga.data.remote.requests

data class AddOwnerRequest(
    val owner: String,
    val semesterId: String
)