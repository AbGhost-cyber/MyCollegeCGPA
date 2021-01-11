package com.crushtech.mycollegecgpa.dialogs

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.crushtech.mycollegecgpa.MainActivity
import com.crushtech.mycollegecgpa.R
import com.crushtech.mycollegecgpa.data.local.entities.Courses
import com.crushtech.mycollegecgpa.databinding.CreateCourseItemsBinding
import com.crushtech.mycollegecgpa.utils.SimpleCustomSnackbar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlin.random.Random

@AndroidEntryPoint
class AddCourseDialogFragment : DialogFragment() {
    private var positiveListener: ((Courses) -> Unit)? = null
    private lateinit var binding: CreateCourseItemsBinding
    fun setPositiveListener(listener: (Courses) -> Unit) {
        positiveListener = listener

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = CreateCourseItemsBinding.inflate(
            LayoutInflater.from(context),
            null,
            false
        )
        val createCourseDialog = Dialog(requireContext())
        createCourseDialog.setContentView(binding.root)

        val courseNameEditText = binding.ccCourseName
        val creditHoursEditText = binding.ccCreditHours
        var grade = "F"
        val createCourse = binding.btnCreateCourse
        val spinner = binding.spinnerSelectGrade

        val spinnerArrayList: ArrayList<String> = ArrayList()
        spinnerArrayList.add("A+")
        spinnerArrayList.add("A-")
        spinnerArrayList.add("B+")
        spinnerArrayList.add("B")
        spinnerArrayList.add("B-")
        spinnerArrayList.add("C+")
        spinnerArrayList.add("C")
        spinnerArrayList.add("C-")
        spinnerArrayList.add("D+")
        spinnerArrayList.add("D")
        spinnerArrayList.add("E/F")

        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            spinnerArrayList
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)


        val spinnerItemListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val clickedItem = parent?.getItemAtPosition(position) as String
                grade = clickedItem
            }

        }
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = spinnerItemListener


        //set up random colors
        val listOfRandomColorStrings = listOf(
            "304FFE", "D50000", "00BFA5", "FFD600", "1F243D",
            "FF6D00", "00B8D4", "DD2C00", "00C853", "2962FF",
            "C51162", "FFAB00", "151722", "000000", "CB8D8D",
            "8B52DB", "2B6689", "643124", "344D3E", "46061F",
            "91AB46", "211F24", "FFC5DF", "D4FFF9", "008D3A"
        )
        val randomPos = Random.nextInt(listOfRandomColorStrings.size)
        val color = listOfRandomColorStrings[randomPos]

        //from arguments
        val courses = arguments?.getSerializable("courses") as Courses?
        courses?.let {
            courseNameEditText.setText(it.courseName)
            creditHoursEditText.setText(it.creditHours.toString())
            spinner.setSelection(spinnerAdapter.getPosition(it.grade))
            createCourse.text = getString(R.string.update_course_string)
        }

        createCourse.setOnClickListener {
            val courseName = courseNameEditText.text.toString()
            val creditHours = creditHoursEditText.text.toString()

            if (courseName.isEmpty() || creditHours.isEmpty()) {
                SimpleCustomSnackbar.make(
                    MainActivity().activityMainBinding.root,
                    "please input all fields", Snackbar.LENGTH_LONG, null,
                    R.drawable.ic_baseline_error_outline_24, "",
                    Color.RED
                )?.show()
                return@setOnClickListener
            }


            val course = if (courses?.id.isNullOrEmpty()) {
                Courses(courseName, creditHours.toFloat(), grade, color, "")
            } else {
                courses?.semesterId?.let { it1 ->
                    Courses(
                        courseName, creditHours.toFloat(), grade, color, it1, id = courses.id
                    )
                }

            }

            positiveListener?.let {
                if (course != null) {
                    it(course)
                }
                createCourseDialog.cancel()
            }
        }
        createCourseDialog.create()
        createCourseDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return createCourseDialog
    }

}