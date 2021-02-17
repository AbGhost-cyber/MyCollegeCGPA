package com.crushtech.myccgpa.data.remote.requests

import com.crushtech.myccgpa.data.local.entities.Courses

data class UpdateCourseRequest(
    val semesterId: String,
    val course: Courses,
    val coursePosition: Int
)