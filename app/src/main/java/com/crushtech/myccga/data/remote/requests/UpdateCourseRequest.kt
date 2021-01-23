package com.crushtech.myccga.data.remote.requests

import com.crushtech.myccga.data.local.entities.Courses

data class UpdateCourseRequest(
    val semesterId: String,
    val course: Courses,
    val coursePosition: Int
)