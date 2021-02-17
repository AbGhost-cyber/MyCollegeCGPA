package com.crushtech.myccgpa.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity(tableName = "grades")
data class GradeClass(
    var APlusGrade: Float = 4F,
    var AMinusGrade: Float = 3.7F,
    var BPlusGrade: Float = 3.3F,
    var BGrade: Float = 3.0F,
    var BMinusGrade: Float = 2.7F,
    var CPlusGrade: Float = 2.3F,
    var CGrade: Float = 2.0F,
    var CMinusGrade: Float = 1.7F,
    var DPlusGrade: Float = 1.3F,
    var DGrade: Float = 1.0F,
    var FOrEGrade: Float = 0.0F,
    @PrimaryKey(autoGenerate = false)
    val id: String = UUID.randomUUID().toString()
) : Serializable