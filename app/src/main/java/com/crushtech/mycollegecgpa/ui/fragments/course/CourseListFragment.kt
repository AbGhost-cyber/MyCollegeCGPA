package com.crushtech.mycollegecgpa.ui.fragments.course

import android.animation.ObjectAnimator
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.crushtech.mycollegecgpa.MainActivity
import com.crushtech.mycollegecgpa.R
import com.crushtech.mycollegecgpa.adapters.CourseAdapter
import com.crushtech.mycollegecgpa.data.local.entities.Courses
import com.crushtech.mycollegecgpa.data.local.entities.GradeClass
import com.crushtech.mycollegecgpa.data.local.entities.Semester
import com.crushtech.mycollegecgpa.databinding.CourseListLayoutBinding
import com.crushtech.mycollegecgpa.dialogs.AddCourseDialogFragment
import com.crushtech.mycollegecgpa.dialogs.DeleteCourseDialogFragment
import com.crushtech.mycollegecgpa.ui.BaseFragment
import com.crushtech.mycollegecgpa.ui.fragments.weights.WeightViewModel
import com.crushtech.mycollegecgpa.utils.Constants.KEY_LOGGED_IN_EMAIL
import com.crushtech.mycollegecgpa.utils.Constants.NO_EMAIL
import com.crushtech.mycollegecgpa.utils.Constants.customRecyclerViewScrollListener
import com.crushtech.mycollegecgpa.utils.Status
import com.crushtech.mycollegecgpa.utils.viewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject


const val ADD_COURSE_DIALOG = "add course dialog"
const val DELETE_COURSE_DIALOG = "delete course dialog"

@AndroidEntryPoint
class CourseListFragment : BaseFragment(R.layout.course_list_layout) {
    private var binding: CourseListLayoutBinding by viewLifecycle()
    private val args: CourseListFragmentArgs by navArgs()

    private val viewModel: CourseViewModel by viewModels()
    private val weightViewModel: WeightViewModel by viewModels()

    private var currentSemester: Semester? = null
    private var currentGradePoints: GradeClass? = null

    private lateinit var courseAdapter: CourseAdapter

    private var authEmail: String? = null

    var isOwner: Boolean = false
    @Inject
    lateinit var sharedPrefs: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CourseListLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    @ExperimentalStdlibApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        weightViewModel.syncGradePoints()
        authEmail = sharedPrefs.getString(
            KEY_LOGGED_IN_EMAIL,
            NO_EMAIL
        ) ?: NO_EMAIL
        (activity as MainActivity).apply {
            hideMainActivityUI()
            showAppBar()
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_chevron_left_24)
            activityMainBinding.titleBarText.text = args.semesterName.capitalize(Locale.ROOT)
        }
        binding.semesterName.setText(args.semesterName)

        if (args.semesterId.isNotEmpty()) {
            viewModel.getSemesterById(args.semesterId)
            viewModel.getCourseList(args.semesterId)
            setupRecyclerView()
            subscribeToObservers()
        }

        binding.fabCreateCourse.setOnClickListener {
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
            if (isOwner) {
                val bundle = Bundle()
                bundle.putSerializable("courses", courses)
                AddCourseDialogFragment().apply {
                    arguments = bundle
                    setPositiveListener {
                        updateCourse(it, "course updated", position)
                    }
                }.show(parentFragmentManager, ADD_COURSE_DIALOG)
            } else {
                showSnackBar(
                    "can't edit, this course is view only!", null,
                    R.drawable.ic_baseline_error_outline_24,
                    "", Color.RED
                )
            }

        }

        binding.fabSaveCourse.setOnClickListener {
            saveSemester()
        }
    }

    private fun setupRecyclerView() {
        binding.courseRv.apply {
            //  itemAnimator = null
            courseAdapter = CourseAdapter(this@CourseListFragment)
            adapter = courseAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addOnScrollListener(
                customRecyclerViewScrollListener(
                    listOf(binding.fabCreateCourse, binding.fabSaveCourse)
                )
            )
        }
    }

    private fun subscribeToObservers() {
        weightViewModel.allGradePoints.observe(viewLifecycleOwner, {
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
                        showSnackBar(
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

        viewModel.semester.observe(viewLifecycleOwner, {
            //return our semester resource for the first time if not handled
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        val semester = result.data!!
                        currentSemester = semester
                        viewModel.updateCourses(semester.courses, semester.id)

                        //play with create and save icons visibility
                        currentSemester?.let { _semester ->
                            if (_semester.owners[0] == authEmail || _semester.owners == listOf(
                                    authEmail
                                )
                            ) {
                                binding.fabSaveCourse.visibility = View.VISIBLE
                                binding.fabCreateCourse.visibility = View.VISIBLE
                                isOwner = true
                            } else {
                                binding.fabSaveCourse.visibility = View.GONE
                                binding.fabCreateCourse.visibility = View.GONE
                                isOwner = false
                            }
                        }
                    }
                    Status.ERROR -> {
                        showSnackBar(
                            result.message ?: "semester not found", null,
                            R.drawable.ic_baseline_error_outline_24, "", Color.RED
                        )
                    }
                    Status.LOADING -> {
                        binding.noCourse.visibility = View.GONE
                        binding.lottieAnimationView.visibility = View.GONE
                        binding.noCourseDes.visibility = View.GONE
                    }
                }
            }
        })

        viewModel.courses.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { courses ->
                            courseAdapter.differ.submitList(courses)
                            currentSemester?.courses = courses
                            viewModel.getCourseList(args.semesterId)
                            if (result.data.isEmpty()) {
                                binding.noCourse.visibility = View.VISIBLE
                                binding.lottieAnimationView.visibility = View.VISIBLE
                                binding.noCourseDes.visibility = View.VISIBLE
                            } else {
                                binding.noCourse.visibility = View.GONE
                                binding.lottieAnimationView.visibility = View.GONE
                                binding.noCourseDes.visibility = View.GONE
                            }
                        }

                    }
                    Status.ERROR -> {
                        showSnackBar(
                            result.message ?: "course not found", null,
                            R.drawable.ic_baseline_error_outline_24, "", Color.RED
                        )
                        binding.noCourse.visibility = View.GONE
                        binding.lottieAnimationView.visibility = View.GONE
                        binding.noCourseDes.visibility = View.GONE
                    }
                    Status.LOADING -> {
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

        val semesterName = binding.semesterName.text.toString()

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
        showSnackBar(
            "semester updated", null,
            R.drawable.ic_baseline_bubble_chart_24, "", Color.BLACK
        )
        //close view if open
            ObjectAnimator.ofFloat(
                courseAdapter.binding?.itemsLayout, "translationX",
                0F
            ).apply {
                duration = 50
                start()
            }
    }

    private fun showCreateCourseDialog() {
        AddCourseDialogFragment().apply {
            setPositiveListener { course ->
                insertCourse(course, "course saved")
            }
        }.show(parentFragmentManager, ADD_COURSE_DIALOG)
    }

    private fun insertCourse(course: Courses, message: String) {
        val semesterId = currentSemester?.id ?: UUID.randomUUID().toString()
        currentGradePoints?.let {
            course.gradesPoints = listOf(it)
        }
        viewModel.insertCourse(course, semesterId)

        showSnackBar(
            message, null,
            R.drawable.ic_baseline_bubble_chart_24,
            "", Color.BLACK
        )
    }

    fun updateCourse(course: Courses, message: String, coursePosition: Int) {
        val semesterId = currentSemester?.id ?: UUID.randomUUID().toString()
        currentGradePoints?.let {
            course.gradesPoints = listOf(it)
        }
        viewModel.updateCourse(course, semesterId, coursePosition)

        showSnackBar(
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
                    showSnackBar(
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

    override fun onDestroy() {
        if (this::courseAdapter.isInitialized) {
            courseAdapter.binding = null
        }
        super.onDestroy()
    }
}

