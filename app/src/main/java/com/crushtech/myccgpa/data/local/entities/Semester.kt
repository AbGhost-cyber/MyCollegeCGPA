package com.crushtech.myccgpa.data.local.entities

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
        val totalHours = courses.map { it.creditHours }.sum()
        val totalPoints = courses.map { it.getQualityPoints() }.sum()
        val gpa = totalPoints / totalHours
        if (gpa.isNaN()) {
            return 0.00
        }
        return (gpa * 100.0).roundToInt() / 100.0
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

fun getCGPA(vararg semesters: Semester): Double {
    val totalGPA = semesters.map { it.getGPA() }.sum()
    val size = semesters.size
    return totalGPA / size
}
