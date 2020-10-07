package com.crushtech.mycollegecgpa.data.remote.requests

import com.crushtech.mycollegecgpa.data.local.entities.Courses

data class AddCourseRequest(
    val semesterId: String,
    val course: Courses
)