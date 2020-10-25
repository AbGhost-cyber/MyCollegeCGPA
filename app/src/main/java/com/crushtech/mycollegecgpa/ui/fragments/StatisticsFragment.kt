package com.crushtech.mycollegecgpa.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.crushtech.mycollegecgpa.MainActivity
import com.crushtech.mycollegecgpa.R
import com.crushtech.mycollegecgpa.ui.BaseFragment
import com.crushtech.mycollegecgpa.ui.fragments.home.HomeViewModel
import com.crushtech.mycollegecgpa.utils.CustomMarkerView
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.statistics_fragment.*

@AndroidEntryPoint
class StatisticsFragment : BaseFragment(R.layout.statistics_fragment) {
    private val viewModel: HomeViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).apply {
            showAppBar()
            hideMainActivityUI()
        }
        setUpObservers()
        setUpBarCharts()
    }

    private fun setUpObservers() {
        viewModel.allSemesters.observe(viewLifecycleOwner, Observer {
            it?.let {
                val result = it.peekContent()
                val semester = result.data
                var totalNumberOfCourses = 0
                var totalNumberOfCreditHours = 0F
                semester?.let { semesters ->
                    semesters.forEach { sem ->
                        totalNumberOfCourses += sem.courses.size
                        val courseString = if (totalNumberOfCourses <= 1) {
                            "$totalNumberOfCourses course"
                        } else {
                            "$totalNumberOfCourses courses"
                        }
                        totalCoursesOffered.text = courseString
                        sem.courses.forEach { course ->
                            totalNumberOfCreditHours += course.creditHours
                            val creditHoursString = if (totalNumberOfCreditHours <= 1) {
                                "$totalNumberOfCreditHours hour"
                            } else {
                                "$totalNumberOfCreditHours hours"
                            }
                            tvCHours.text = creditHoursString
                        }
                    }
                    val allGPA =
                        semesters.indices.map { i ->
                            BarEntry(i.toFloat(), semesters[i].getGPA().toFloat())
                        }
                    val typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.averia_libre)


                    val colorList = listOf(
                        getColor(requireContext(), R.color.colorPrimary),
                        getColor(requireContext(), R.color.colorAccent)
                    )

                    val barDataSet = BarDataSet(allGPA, "GPA per semester").apply {
                        valueTextColor = Color.BLACK
                        valueTypeface = typeface
                        valueTextSize = 13f
                        colors = colorList

                    }

                    barChart.data = BarData(barDataSet)
                    barChart.invalidate()
                    barChart.marker =
                        CustomMarkerView(semesters, requireContext(), R.layout.marker_view)
                    barChart.description.isEnabled = false
                }
            }
        })
    }

    private fun setUpBarCharts() {
        barChart.xAxis.apply {
            position = XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.BLACK
            textColor = Color.BLACK
            setDrawGridLines(false)
        }
        barChart.axisLeft.apply {
            axisLineColor = Color.BLACK
            textColor = Color.BLACK
            setDrawGridLines(false)

        }
        barChart.axisRight.apply {
            axisLineColor = Color.BLACK
            textColor = Color.BLACK
            setDrawGridLines(false)

        }
    }
}