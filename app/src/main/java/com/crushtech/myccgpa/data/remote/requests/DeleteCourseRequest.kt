package com.crushtech.myccgpa.data.remote.requests

data class DeleteCourseRequest(
    val id: String,
    val semesterId: String
)