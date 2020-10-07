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
    @PrimaryKey(autoGenerate = false)
    val id: String = UUID.randomUUID().toString()
) : Serializable {
    fun getQualityPoints(): Float {
        return creditHours * getGradePoints()
    }

    private fun getGradePoints(): Float {
        return when (grade) {
            "A" -> 4F
            "B+" -> 3.5F
            "B-" -> 3.0F
            "C+" -> 2.5F
            "C-" -> 2.0F
            "D+" -> 1.5F
            "D-" -> 1.0F
            "F" -> 0.0F
            else -> 0.0F
        }
    }
}


////POJO
//data class SemesterWithCourses(
//    @Embedded
//    val semester: Semester,
//    @Relation(parentColumn = "sId",
//        entityColumn = "semesterId",
//        entity = Courses::class
//    )
//    val courses:ArrayList<Courses>
//
//)