package com.crushtech.mycollegecgpa.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Locally_deleted_course_ids")
data class LocallyDeletedCourseId(
    @PrimaryKey(autoGenerate = false)
    val deletedCourseId: String,
    val deletedCourseSemesterId: String
)