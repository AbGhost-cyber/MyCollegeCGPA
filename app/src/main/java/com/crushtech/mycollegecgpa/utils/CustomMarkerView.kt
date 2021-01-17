package com.crushtech.mycollegecgpa.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import com.crushtech.mycollegecgpa.data.local.entities.Semester
import com.crushtech.mycollegecgpa.databinding.MarkerViewBinding
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF


@SuppressLint("ViewConstructor")
class CustomMarkerView(
    val semester: List<Semester>,
    c: Context,
    layoutId: Int
) : MarkerView(c, layoutId) {
    private var binding = MarkerViewBinding.inflate(
        LayoutInflater.from(context), this, true
    )

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
        binding.tvSemesterName.setText(semesterName)

    }
}