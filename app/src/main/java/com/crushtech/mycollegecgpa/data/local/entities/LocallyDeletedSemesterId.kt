package com.crushtech.mycollegecgpa.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Locally_deleted_semester_ids")
data class LocallyDeletedSemesterId(
    @PrimaryKey(autoGenerate = false)
    val deletedSemesterId: String
)