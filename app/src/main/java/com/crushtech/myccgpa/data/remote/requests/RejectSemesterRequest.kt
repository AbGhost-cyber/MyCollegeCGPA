package com.crushtech.myccgpa.data.remote.requests

import com.crushtech.myccgpa.data.local.entities.SemesterRequests

data class RejectSemesterRequest(
    val semesterRequests: SemesterRequests,
    val receiver: String
)