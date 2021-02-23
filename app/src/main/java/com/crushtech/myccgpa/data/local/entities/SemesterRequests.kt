package com.crushtech.myccgpa.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity(tableName = "semester_requests")
data class SemesterRequests(
    val owner: String,
    val semesterId: String,
    var state: STATE,
    @PrimaryKey(autoGenerate = false)
    val id: String = UUID.randomUUID().toString()
) : Serializable

enum class STATE {
    ACCEPTED, PENDING, REJECTED
}