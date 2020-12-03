package com.crushtech.mycollegecgpa.ui.fragments.course

import android.animation.ObjectAnimator
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.crushtech.mycollegecgpa.MainActivity
import com.crushtech.mycollegecgpa.R
import com.crushtech.mycollegecgpa.adapters.CourseAdapter
import com.crushtech.mycollegecgpa.data.local.entities.Courses
import com.crushtech.mycollegecgpa.data.local.entities.GradeClass
import com.crushtech.mycollegecgpa.data.local.entities.Semester
import com.crushtech.mycollegecgpa.dialogs.AddCourseDialogFragment
import com.crushtech.mycollegecgpa.dialogs.DeleteCourseDialogFragment
import com.crushtech.mycollegecgpa.ui.BaseFragment
import com.crushtech.mycollegecgpa.ui.fragments.weights.WeightViewModel
import com.crushtech.mycollegecgpa.utils.Constants
import com.crushtech.mycollegecgpa.utils.Constants.GPA_MAX
import com.crushtech.mycollegecgpa.utils.Constants.KEY_LOGGED_IN_EMAIL
import com.crushtech.mycollegecgpa.utils.Constants.NO_EMAIL
import com.crushtech.mycollegecgpa.utils.Constants.customRecyclerViewScrollListener
import com.crushtech.mycollegecgpa.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.course_items.view.*
import kotlinx.android.synthetic.main.course_list_layout.*
import java.util.*
import javax.inject.Inject


const val ADD_COURSE_DIALOG = "add course dialog"
const val DELETE_COURSE_DIALOG = "delete course dialog"

@AndroidEntryPoint
class CourseListFragment : BaseFragment(R.layout.course_list_layout) {
    private val args: CourseListFragmentArgs by navArgs()

    private val viewModel: CourseViewModel by viewModels()
    private val weightViewModel: WeightViewModel by viewModels()

    private var currentSemester: Semester? = null
    private var currentGradePoints: GradeClass? = null

    private lateinit var courseAdapter: CourseAdapter

    private var firsTimeOpen = false

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (!(sharedPrefs.contains(Constants.COURSE_FIRST_TIME_OPEN))) {
            firsTimeOpen = true
            sharedPrefs.edit().putBoolean(Constants.COURSE_FIRST_TIME_OPEN, true).apply()
        }
        return inflater.inflate(R.layout.course_list_layout, container, false)
    }

    @ExperimentalStdlibApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        weightViewModel.syncGradePoints()
        (activity as MainActivity).apply {
            hideMainActivityUI()
            showAppBar()
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_chevron_left_24)
        }
        requireActivity().titleBarText.text = args.semesterName.capitalize(Locale.ROOT)
        semesterName.setText(args.semesterName)

        if (args.semesterId.isNotEmpty()) {
            viewModel.getSemesterById(args.semesterId)
            viewModel.getCourseList(args.semesterId)
            setupRecyclerView()
            subscribeToObservers()
        }
        fabCreateCourse.setOnClickListener {
            showCreateCourseDialog()
        }
        if (savedInstanceState != null) {
            val addCourseDialog = parentFragmentManager.findFragmentByTag(ADD_COURSE_DIALOG)
                    as AddCourseDialogFragment?
            addCourseDialog?.setPositiveListener { course ->
                insertCourse(course, "saved")
            }
        }

        courseAdapter.setOnItemClickListener { courses, position ->
            val bundle = Bundle()
            bundle.putSerializable("courses", courses)
            AddCourseDialogFragment().apply {
                arguments = bundle
                setPositiveListener {
                    updateCourse(it, "course updated", position)
                }
            }.show(parentFragmentManager, ADD_COURSE_DIALOG)
        }

        fabSaveCourse.setOnClickListener {
            currentSemester?.let {
                if (it.getGPA() > GPA_MAX) {
                    showSnackbar(
                        "it seems your gpa is much higher than required, please edit", null,
                        R.drawable.ic_baseline_error_outline_24,
                        "", Color.RED
                    )
                }
            }
            saveSemester()
        }
    }

    private fun setupRecyclerView() {
        courseRv.apply {
            itemAnimator = null
            courseAdapter = CourseAdapter(this@CourseListFragment)
            adapter = courseAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addOnScrollListener(
                customRecyclerViewScrollListener(
                    listOf(fabCreateCourse, fabSaveCourse)
                )
            )
        }
    }

    private fun subscribeToObservers() {
        weightViewModel.allGradePoints.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        val gradePoints = result.data
                        currentSemester?.courses?.forEach { course ->
                            if (course.gradesPoints.isNullOrEmpty()) {
                                gradePoints?.let { _gradepoints ->
                                    weightViewModel.insertGradesPoints(_gradepoints)
                                }
                            }
                        }
                        currentGradePoints = gradePoints
                    }
                    Status.ERROR -> {
                        showSnackbar(
                            result.message ?: "course not found", null,
                            R.drawable.ic_baseline_error_outline_24,
                            "", Color.RED
                        )
                    }
                    Status.LOADING -> {
                        //No OP
                    }
                }
            }

        })

        viewModel.semester.observe(viewLifecycleOwner, Observer {
            //return our semester resource for the first time if not handled
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        courseAdapter.showShimmer = false
                        val semester = result.data!!
                        if (firsTimeOpen) {
                            viewModel.insertCourses(semester.courses, semester.id)
                        }
                        currentSemester?.let { sem ->
                            if (sem.courses.isEmpty() && semester.courses.isNotEmpty()) {
                                viewModel.insertCourses(semester.courses, semester.id)
                            }
                        }

                        currentSemester = semester

                    }
                    Status.ERROR -> {
                        courseAdapter.showShimmer = false
                        showSnackbar(
                            result.message ?: "semester not found", null,
                            R.drawable.ic_baseline_error_outline_24, "", Color.RED
                        )
                    }
                    Status.LOADING -> {
                        courseAdapter.showShimmer = true
                        no_course.visibility = View.GONE
                        lottieAnimationView.visibility = View.GONE
                        no_course_des.visibility = View.GONE
                    }
                }
            }
        })

        viewModel.courses.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        courseAdapter.showShimmer = false
                        result.data?.let { courses ->
                            courseAdapter.differ.submitList(courses)
                            currentSemester?.courses = courses
                            viewModel.getCourseList(args.semesterId)
                            if (result.data.isEmpty()) {
                                no_course.visibility = View.VISIBLE
                                lottieAnimationView.visibility = View.VISIBLE
                                no_course_des.visibility = View.VISIBLE
                            } else {
                                no_course.visibility = View.GONE
                                lottieAnimationView.visibility = View.GONE
                                no_course_des.visibility = View.GONE
                            }
                        }

                    }
                    Status.ERROR -> {
                        courseAdapter.showShimmer = false
                        showSnackbar(
                            result.message ?: "course not found", null,
                            R.drawable.ic_baseline_error_outline_24, "", Color.RED
                        )
                        no_course.visibility = View.GONE
                        lottieAnimationView.visibility = View.GONE
                        no_course_des.visibility = View.GONE
                    }
                    Status.LOADING -> {
                        courseAdapter.showShimmer = false
                    }
                }
            }
        })

    }


    private fun saveSemester() {
        val authEmail = sharedPrefs.getString(
            KEY_LOGGED_IN_EMAIL,
            NO_EMAIL
        ) ?: NO_EMAIL

        val semesterName = semesterName.text.toString()

        if (semesterName.isEmpty()) {
            return
        }
        //if semester id is null, generate a new random UUID
        val id = currentSemester?.id ?: UUID.randomUUID().toString()

        //if owners if null, create a new list of owners with the current user as owner
        val owners = currentSemester?.owners ?: listOf(authEmail)

        //if semester's courses isn't null, then insert it
        val semester = currentSemester?.courses?.let { course ->
            Semester(
                semesterName = semesterName,
                owners = owners, id = id, courses = course
            )

        }
        if (semester != null) {
            viewModel.insertSemester(semester)
        }
        showSnackbar(
            "semester updated", null,
            R.drawable.ic_baseline_bubble_chart_24, "", Color.BLACK
        )
        //close view if open
        courseAdapter.itemView?.let {
            ObjectAnimator.ofFloat(
                it.itemsLayout, "translationX",
                0F
            ).apply {
                duration = 50
                start()
            }
        }
    }

    private fun showCreateCourseDialog() {
        AddCourseDialogFragment().apply {
            setPositiveListener { course ->
                insertCourse(course, "course saved")
            }
        }.show(parentFragmentManager, ADD_COURSE_DIALOG)
    }

    fun insertCourse(course: Courses, message: String) {
        val semesterId = currentSemester?.id ?: UUID.randomUUID().toString()
        course.semesterId = semesterId
        currentGradePoints?.let {
            course.gradesPoints = listOf(it)
        }
        viewModel.insertCourse(course, semesterId)

        showSnackbar(
            message, null,
            R.drawable.ic_baseline_bubble_chart_24,
            "", Color.BLACK
        )
    }

    fun updateCourse(course: Courses, message: String, coursePosition: Int) {
        val semesterId = currentSemester?.id ?: UUID.randomUUID().toString()
        course.semesterId = semesterId
        currentGradePoints?.let {
            course.gradesPoints = listOf(it)
        }
        viewModel.updateCourse(course, semesterId, coursePosition)

        showSnackbar(
            message, null,
            R.drawable.ic_baseline_bubble_chart_24,
            "", Color.BLACK
        )
    }

    fun showDeleteCourseDialog(course: Courses) {
        val bundle = Bundle()
        bundle.putSerializable("courses", course)
        DeleteCourseDialogFragment().apply {
            arguments = bundle
            setPositiveListener { deleted ->
                if (deleted) {
                    deleteCourse(course)
                    showSnackbar(
                        "course deleted", null,
                        R.drawable.ic_baseline_bubble_chart_24,
                        "", Color.BLACK
                    )
                }
            }
        }.show(parentFragmentManager, DELETE_COURSE_DIALOG)
    }

    private fun deleteCourse(course: Courses) {
        currentSemester?.let {
            viewModel.deleteCourse(course.id, it.id)
        }
    }
}