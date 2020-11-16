package com.crushtech.mycollegecgpa.ui.fragments.course

import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crushtech.mycollegecgpa.MainActivity
import com.crushtech.mycollegecgpa.R
import com.crushtech.mycollegecgpa.adapters.CourseAdapter
import com.crushtech.mycollegecgpa.data.local.entities.Courses
import com.crushtech.mycollegecgpa.data.local.entities.GradeClass
import com.crushtech.mycollegecgpa.data.local.entities.Semester
import com.crushtech.mycollegecgpa.dialogs.AddCourseDialogFragment
import com.crushtech.mycollegecgpa.ui.BaseFragment
import com.crushtech.mycollegecgpa.ui.fragments.weights.WeightViewModel
import com.crushtech.mycollegecgpa.utils.Constants.KEY_LOGGED_IN_EMAIL
import com.crushtech.mycollegecgpa.utils.Constants.NO_EMAIL
import com.crushtech.mycollegecgpa.utils.Constants.setupDecorator
import com.crushtech.mycollegecgpa.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.course_list_layout.*
import java.util.*
import javax.inject.Inject


const val ADD_COURSE_DIALOG = "add course dialog"

@AndroidEntryPoint
class CourseListFragment : BaseFragment(R.layout.course_list_layout) {
    private val args: CourseListFragmentArgs by navArgs()

    private val viewModel: CourseViewModel by viewModels()
    private val weightViewModel: WeightViewModel by viewModels()

    private var currentSemester: Semester? = null
    private var currentGradePoints: GradeClass? = null

    private lateinit var courseAdapter: CourseAdapter


    @Inject
    lateinit var sharedPrefs: SharedPreferences

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

        courseAdapter.setOnItemClickListener { courses ->
            val bundle = Bundle()
            bundle.putSerializable("courses", courses)
            AddCourseDialogFragment().apply {
                arguments = bundle
                setPositiveListener {
                    insertCourse(it, "course updated")
                }
            }.show(parentFragmentManager, ADD_COURSE_DIALOG)
        }
    }

    private fun setupRecyclerView() {
        courseRv.apply {
            courseAdapter = CourseAdapter()
            adapter = courseAdapter
            layoutManager = LinearLayoutManager(requireContext())
            ItemTouchHelper(itemTouchHelperCallback)
                .attachToRecyclerView(this)


        }
    }

    private fun subscribeToObservers() {
        weightViewModel.allGradePoints.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        val gradePoints = result.data!!
                        currentSemester?.courses?.forEach { course ->
                            if (course.gradesPoints.isNullOrEmpty()) {
                                weightViewModel.insertGradesPoints(gradePoints)
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
                        val semester = result.data!!
                        if (currentSemester?.courses.isNullOrEmpty()) {
                            viewModel.insertCourses(semester.courses, semester.id)
                        }
                        currentSemester = semester

                    }
                    Status.ERROR -> {
                        showSnackbar(
                            result.message ?: "semester not found", null,
                            R.drawable.ic_baseline_error_outline_24, "", Color.RED
                        )
                    }
                    Status.LOADING -> {
                        showSnackbar(
                            "loading please wait....", null,
                            R.drawable.ic_baseline_emoji_emotions_24, "", Color.BLACK
                        )
                    }
                }
            }
        })

        viewModel.courses.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { courses ->
                            courseAdapter.differ.submitList(courses)
                            currentSemester?.courses = courses
                            viewModel.getCourseList(args.semesterId)
                            if (result.data.isEmpty()) {
                                no_course.visibility = View.VISIBLE
                            } else {
                                no_course.visibility = View.GONE
                            }
                            if (no_course.isVisible) {
                                swipeText.visibility = View.GONE
                            } else {
                                swipeText.visibility = View.VISIBLE
                            }
                        }

                    }
                    Status.ERROR -> {
                        showSnackbar(
                            result.message ?: "course not found", null,
                            R.drawable.ic_baseline_error_outline_24, "", Color.RED
                        )
                    }
                    Status.LOADING -> {
                        /* NO-OP */
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


    private val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.LEFT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.layoutPosition
            val course = courseAdapter.differ.currentList[position]
            viewModel.deleteCourse(course.id, course.semesterId)
            courseAdapter.notifyItemRemoved(position)
            val snackListener = View.OnClickListener {
                viewModel.insertCourse(course, course.semesterId)
                showSnackbar(
                    "course inserted", null,
                    R.drawable.ic_baseline_delete_24,
                    "", Color.BLACK
                )
                viewModel.deleteLocallyDeletedCourseId(course.id)
            }
            showSnackbar(
                "course deleted", snackListener, R.drawable.ic_baseline_delete_24,
                "Undo", Color.BLACK
            )
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            setupDecorator(
                c, recyclerView, viewHolder, dX,
                dY, actionState, isCurrentlyActive
            )

            super.onChildDraw(
                c,
                recyclerView,
                viewHolder,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )

        }

    }


    override fun onPause() {
        saveSemester()
        super.onPause()
    }
}