package com.crushtech.myccga.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

@Entity(tableName = "Semester")
data class Semester(
    var courses: List<Courses> = listOf(),
    val owners: List<String> = listOf(),
    val semesterName: String = "semester one",
    @Expose(deserialize = false, serialize = false)
    var isSynced: Boolean = false,

    @PrimaryKey(autoGenerate = false)
    val id: String = UUID.randomUUID().toString()
) {

    fun getGPA(): Double {
        var totalHours = 0F
        var totalPoints = 0F
        courses.forEach {
            totalHours += it.creditHours
            totalPoints += it.getQualityPoints()

        }

        val GPA = totalPoints / totalHours
        if (GPA.isNaN()) {
            return 0.00
        }
        return (GPA * 100.0).roundToInt() / 100.0
    }


    fun getThreeCoursesName(): String {
        val threeCourses = ArrayList<String>()
        courses.forEach {
            if (threeCourses.size < 3) {
                threeCourses.add(it.courseName)
            }

        }
        return threeCourses.joinToString(", ")
    }
}


fun getCGPA(semester: Triple<Semester, Semester, Semester>): Double {
    val firstSemesterGPA = semester.first.getGPA()
    val secondSemesterGPA = semester.second.getGPA()
    val thirdSemesterGPA = semester.third.getGPA()
    val totalGPA = firstSemesterGPA + secondSemesterGPA + thirdSemesterGPA
    return totalGPA / 3
}


