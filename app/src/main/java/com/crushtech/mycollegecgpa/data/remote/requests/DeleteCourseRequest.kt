package com.crushtech.mycollegecgpa.data.remote.requests

data class DeleteCourseRequest(
    val id: String,
    val semesterId: String
)