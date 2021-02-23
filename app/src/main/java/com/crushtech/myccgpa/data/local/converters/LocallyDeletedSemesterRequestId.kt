package com.crushtech.myccgpa.data.local.converters

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Locally_deleted_semester_request_ids")
data class LocallyDeletedSemesterRequestId(
    @PrimaryKey(autoGenerate = false)
    val deletedSemReqId: String
)