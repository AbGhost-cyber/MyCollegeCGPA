package com.crushtech.mycollegecgpa.ui.fragments.course

import android.content.SharedPreferences
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
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
import com.crushtech.mycollegecgpa.data.local.entities.Semester
import com.crushtech.mycollegecgpa.dialogs.AddCourseDialogFragment
import com.crushtech.mycollegecgpa.ui.BaseFragment
import com.crushtech.mycollegecgpa.utils.Constants.KEY_LOGGED_IN_EMAIL
import com.crushtech.mycollegecgpa.utils.Constants.NO_EMAIL
import com.crushtech.mycollegecgpa.utils.Constants.setupDecorator
import com.crushtech.mycollegecgpa.utils.Status
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.course_list_layout.*
import java.util.*
import javax.inject.Inject

const val ADD_COURSE_DIALOG = "add course dialog"

@AndroidEntryPoint
class CourseListFragment : BaseFragment(R.layout.course_list_layout) {
    private val args: CourseListFragmentArgs by navArgs()

    private val viewModel: CourseViewModel by viewModels()

    private var currentSemester: Semester? = null

    private lateinit var courseAdapter: CourseAdapter

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).apply {
            hideMainActivityUI()
            showAppBar()
            supportActionBar?.title = args.semesterName
        }
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

    private fun setupRecyclerView() = courseRv.apply {
        courseAdapter = CourseAdapter()
        adapter = courseAdapter
        layoutManager = LinearLayoutManager(requireContext())
        ItemTouchHelper(itemTouchHelperCallback)
            .attachToRecyclerView(this)
    }

    private fun subscribeToObservers() {
        viewModel.semester.observe(viewLifecycleOwner, Observer {
            //return our semester resource for the first time if not handled
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        val semester = result.data!!
                        currentSemester = semester
                    }
                    Status.ERROR -> {
                        showSnackbar(
                            result.message ?: "semester not found"
                        )
                    }
                    Status.LOADING -> {
                        /* NO-OP */
                    }
                }
            }
        })

        viewModel.courses.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        courseAdapter.differ.submitList(result.data)
                        currentSemester?.courses = result.data!!
                        viewModel.getCourseList(currentSemester?.id ?: args.semesterId)
                        if (result.data.isEmpty()) {
                            no_course.visibility = View.VISIBLE
                        } else {
                            no_course.visibility = View.GONE
                        }

                    }
                    Status.ERROR -> {
                        showSnackbar(
                            result.message ?: "course not found"
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
        val semester = currentSemester?.courses?.let { courses ->
            Semester(
                semesterName = semesterName,
                owners = owners, id = id, courses = courses
            )
        }
        if (semester != null) {
            viewModel.insertSemester(semester)
        }
        showSnackbar("semester updated")
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
        viewModel.insertCourse(course, semesterId)
        showSnackbar(message)
    }


    private val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.LEFT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.layoutPosition
            val course = courseAdapter.differ.currentList[position]
            viewModel.deleteCourse(course.id, course.semesterId)
            Snackbar.make(
                requireView(), "course deleted",
                Snackbar.LENGTH_LONG
            ).apply {
                setAction("Undo") {
                    viewModel.insertCourse(course, course.semesterId)
                    viewModel.deleteLocallyDeletedCourseId(course.id)
                }
                show()
            }
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
                c, recyclerView, viewHolder, dX, dY,
                actionState, isCurrentlyActive, "Delete",
                R.drawable.ic_baseline_delete_24, android.R.color.holo_red_dark,
                requireContext()
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