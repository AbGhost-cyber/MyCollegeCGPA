package com.crushtech.mycollegecgpa.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.crushtech.mycollegecgpa.MainActivity
import com.crushtech.mycollegecgpa.R
import com.crushtech.mycollegecgpa.adapters.AboutAppAdapter
import com.crushtech.mycollegecgpa.ui.BaseFragment
import kotlinx.android.synthetic.main.about_app_layout.*
import kotlinx.android.synthetic.main.activity_main.*

 class AboutAppItems(val title: String, val subItem: String){
     var isExpanded = false
 }

class AboutAppFragment : BaseFragment(R.layout.about_app_layout) {
    private lateinit var aboutAppItemsAdapter: AboutAppAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).apply {
            hideMainActivityUI()
            showAppBar()
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_chevron_left_24)
            titleBarText.text = getString(R.string.FAQs)

            setUpRecyclerview()
        }
        aboutAppItemsAdapter.setOnItemClickListener {

        }
    }

    private fun setUpRecyclerview() = aboutAppRv.apply {
        aboutAppItemsAdapter = AboutAppAdapter()
        adapter = aboutAppItemsAdapter
        (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        layoutManager = LinearLayoutManager(requireContext())
        addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        aboutAppItemsAdapter.differ.submitList(initDataForRv())
        setHasFixedSize(true)
    }

    private fun initDataForRv(): List<AboutAppItems> {
        val aboutAppItemsList = ArrayList<AboutAppItems>()
        aboutAppItemsList.add(
            AboutAppItems(
                "What is this app about?",
                getString(R.string.about_mycollege_cgpa)
            ))
        aboutAppItemsList.add(
            AboutAppItems(
                "How do i create a semester?",
               getString(R.string.how_to_create_a_semester)
            ))
        aboutAppItemsList.add(
            AboutAppItems(
                "How to create a course for a semester?",
               getString(R.string.how_to_create_a_course)
            ))
        aboutAppItemsList.add(
            AboutAppItems(
                "What is the limit for a GPA per semester?",
               getString(R.string.gpa_limit_per_semester)
            ))
        aboutAppItemsList.add(
            AboutAppItems(
                "What is a GPA?",
                getString(R.string.what_is_a_gpa)
            ))
        aboutAppItemsList.add(
            AboutAppItems(
                "What does a credit hour mean?",
                getString(R.string.what_is_credit_hours)
            ))
        aboutAppItemsList.add(
            AboutAppItems(
                "how is my gpa calculated?",
               getString(R.string.how_is_my_gpa_calculated)
            ))
        aboutAppItemsList.add(
            AboutAppItems(
                "what are grade/quality points?",
                getString(R.string.what_are_quality_points)
            ))
        aboutAppItemsList.add(
            AboutAppItems(
                "How do i share a semester?",
                "navigate to home screen -> swipe left on the semester you wish to share -> input the receiver's email. please note, the receiver must have an existing account on the app."
            ))
        aboutAppItemsList.add(
            AboutAppItems(
                "what happens to my data if i delete this app mistakenly or not?",
               getString(R.string.what_happensto_my_data)
            ))
        aboutAppItemsList.add(
            AboutAppItems(
                "how can i increase my pdf download coins?",
                getString(R.string.how_to_increase_pdf_downloads)
            ))
        aboutAppItemsList.add(
            AboutAppItems(
                "can i access my data on multiple devices?",
                getString(R.string.can_i_access_on_multiple_device)
            ))
        aboutAppItemsList.add(
            AboutAppItems(
                "the grading system in the app doesn't correspond to that of my college",
                getString(R.string.how_to_edit_grade_points)
            ))

         return aboutAppItemsList
    }
}

