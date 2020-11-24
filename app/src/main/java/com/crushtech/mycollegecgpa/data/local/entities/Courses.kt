package com.crushtech.mycollegecgpa.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity(tableName = "courses")
data class Courses(
    val courseName: String,
    val creditHours: Float,
    val grade: String,
    val color: String,
    var semesterId: String,
    var gradesPoints: List<GradeClass> = listOf(),
    @PrimaryKey(autoGenerate = false)
    val id: String = UUID.randomUUID().toString()
) : Serializable {

    fun getQualityPoints(): Float {
        return creditHours * getGradePoints()
    }

    private fun getGradePoints(): Float {
        //i really had to make gradepoint a list
        // because of room typeconverter issues
        gradesPoints.forEach { gradesPoints ->
            return when (grade) {
                "A+" -> gradesPoints.APlusGrade
                "A-" -> gradesPoints.AMinusGrade
                "B+" -> gradesPoints.BPlusGrade
                "B" -> gradesPoints.BGrade
                "B-" -> gradesPoints.BMinusGrade
                "C+" -> gradesPoints.CPlusGrade
                "C" -> gradesPoints.CGrade
                "C-" -> gradesPoints.CMinusGrade
                "D+" -> gradesPoints.DPlusGrade
                "D" -> gradesPoints.DGrade
                "E/F" -> gradesPoints.FOrEGrade
                else -> gradesPoints.FOrEGrade
            }
        }
        return 0.0F
    }
}

