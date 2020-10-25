package com.crushtech.mycollegecgpa.utils

import android.annotation.SuppressLint
import android.content.Context
import com.crushtech.mycollegecgpa.data.local.entities.Semester
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.marker_view.view.*


@SuppressLint("ViewConstructor")
class CustomMarkerView(
    val semester: List<Semester>,
    c: Context,
    layoutId: Int
) : MarkerView(c, layoutId) {

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if (e == null) {
            return
        }
        val currentSemesterId = e.x.toInt()
        val currentSemester = semester[currentSemesterId]


        val semesterName = currentSemester.semesterName
        tvSemesterName.setText(semesterName)


        val courses = currentSemester.getThreeCoursesName()
        tvThreeCourse.setText(courses)


        var totalCreditHours = 0F
        currentSemester.courses.forEach {
            totalCreditHours += it.creditHours
            tvTotalCreditHours.setText(totalCreditHours.toString())
        }
        val totalCourses = currentSemester.courses.size
        tvTotalCourses.setText(totalCourses.toString())

        val GPA = currentSemester.getGPA()
        tvGPA.setText(GPA.toString())

    }
}