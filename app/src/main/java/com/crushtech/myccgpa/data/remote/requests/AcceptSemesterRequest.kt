package com.crushtech.myccgpa.data.remote.requests

import com.crushtech.myccgpa.data.local.entities.SemesterRequests

data class AcceptSemesterRequest(
    val semesterRequests: SemesterRequests,
    val receiver: String
)