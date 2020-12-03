package com.crushtech.mycollegecgpa.data.remote.requests

import com.crushtech.mycollegecgpa.data.local.entities.Courses

data class UpdateCourseRequest(
    val semesterId: String,
    val course: Courses,
    val coursePosition: Int
)