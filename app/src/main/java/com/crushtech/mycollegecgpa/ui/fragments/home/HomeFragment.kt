package com.crushtech.mycollegecgpa.ui.fragments.home

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.ImageView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.crushtech.mycollegecgpa.MainActivity
import com.crushtech.mycollegecgpa.R
import com.crushtech.mycollegecgpa.adapters.SemesterAdapter
import com.crushtech.mycollegecgpa.data.local.entities.Semester
import com.crushtech.mycollegecgpa.dialogs.AddSemesterDialogFragment
import com.crushtech.mycollegecgpa.ui.BaseFragment
import com.crushtech.mycollegecgpa.utils.Constants
import com.crushtech.mycollegecgpa.utils.Constants.KEY_LOGGED_IN_EMAIL
import com.crushtech.mycollegecgpa.utils.Constants.KEY_USERNAME
import com.crushtech.mycollegecgpa.utils.Constants.NO_EMAIL
import com.crushtech.mycollegecgpa.utils.Status
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.home_layout.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

const val ADD_SEMESTER_DIALOG = "add semester dialog"

@AndroidEntryPoint
class HomeFragment : BaseFragment(R.layout.home_layout) {
    @Inject
    lateinit var sharedPrefs: SharedPreferences

    private lateinit var semesterAdapter: SemesterAdapter
    private lateinit var bestSemesterAdapter: SemesterAdapter

    private val homeViewModel: HomeViewModel by viewModels()

    private val swipingItem = MutableLiveData(false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).hideAppBar()
        (activity as MainActivity).showMainActivityUI()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        val username = "Hello, ${getCurrentUserName()}"
        userName.text = username
        currentDate.text = getFormattedDate()

        if (savedInstanceState != null) {
            val addSemesterDialog = parentFragmentManager.findFragmentByTag(ADD_SEMESTER_DIALOG)
                    as AddSemesterDialogFragment?
            addSemesterDialog?.setPositiveListener { semesterName ->
                insertSemester(semesterName)
            }
        }
        setupRecyclerView()
        setUpBestSemesterRecyclerView()
        setupSwipeRefreshLayout()
        subscribeToObservers()

        (activity as MainActivity).addSemester.setOnClickListener {
            showCreateSemesterDialog()
        }

        val cancelIcon = semesterSearch.findViewById<ImageView>(R.id.search_close_btn)
        cancelIcon.setColorFilter(R.color.colorPrimary)
        cancelIcon.setOnClickListener {
            //clear and reload items
            semesterSearch.setQuery("", false)
            subscribeToObservers()
        }

        semesterSearch.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(newText: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    semesterAdapter.filter.filter(it)
                }
                return false
            }
        })



        semesterAdapter.setOnItemClickListener { semester ->
            findNavController().navigate(
                HomeFragmentDirections
                    .actionHomeFragmentToCourseListFragment(
                        semester.id,
                        semester.semesterName
                    )
            )
        }
    }

    private fun setupRecyclerView() = rvAllSemester.apply {
        semesterAdapter = SemesterAdapter()
        adapter = semesterAdapter
        layoutManager = LinearLayoutManager(requireContext())
        ItemTouchHelper(itemTouchHelperCallback)
            .attachToRecyclerView(this)
    }

    private fun setUpBestSemesterRecyclerView() = rvBestSemester.apply {
        bestSemesterAdapter = SemesterAdapter()
        adapter = bestSemesterAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun getBestSemester(semester: List<Semester>): Semester? {
        return semester.maxBy {
            it.getGPA()
        }
    }

    private fun subscribeToObservers() {
        homeViewModel.allSemesters.observe(viewLifecycleOwner, Observer {
            it?.let { event ->
                val results = event.peekContent()
                when (results.status) {
                    Status.SUCCESS -> {
                        semesterAdapter.differ.submitList(results.data!!)
                        if (results.data.isNotEmpty()) {
                            bestSemesterAdapter.differ.submitList(
                                listOf(getBestSemester(results.data))
                            )
                        }
                        checkForEmptyState(results.data)

                        swipeRefreshLayout.isRefreshing = false

                    }
                    Status.ERROR -> {
                        event.getContentIfNotHandled()?.let { error ->
                            error.message?.let { message ->
                                showSnackbar(message)
                            }
                        }
                        results.data?.let { semester ->
                            semesterAdapter.differ.submitList(semester)
                            checkForEmptyState(semester)
                            swipeRefreshLayout.isRefreshing = false
                        }
                    }
                    Status.LOADING -> {
                        results.data?.let { semester ->
                            semesterAdapter.differ.submitList(semester)
                            checkForEmptyState(semester)
                        }
                        swipeRefreshLayout.isRefreshing = true
                    }
                }
            }
        })
        swipingItem.observe(viewLifecycleOwner, Observer {
            swipeRefreshLayout.isEnabled = !it
        })
    }

    private fun showCreateSemesterDialog() {
        AddSemesterDialogFragment().apply {
            setPositiveListener {
                insertSemester(it)
            }
        }.show(parentFragmentManager, ADD_SEMESTER_DIALOG)
    }

    private fun insertSemester(semesterName: String) {
        val authEmail = sharedPrefs.getString(
            KEY_LOGGED_IN_EMAIL,
            NO_EMAIL
        ) ?: NO_EMAIL
        if (semesterName.isEmpty()) {
            return
        }
        homeViewModel.insertSemester(
            Semester(
                semesterName = semesterName,
                owners = listOf(authEmail)
            )
        )
        showSnackbar("semester created")

    }

    private fun getCurrentUserName(): String {
        return sharedPrefs.getString(
            KEY_USERNAME,
            Constants.NO_USERNAME
        ) ?: Constants.NO_USERNAME
    }

    private fun checkForEmptyState(semesterList: List<Semester>) {
        if (semesterList.isNullOrEmpty()) {
            bestSemester.visibility = INVISIBLE
            bestSemesterText2.visibility = INVISIBLE
            no_semester_txt.visibility = VISIBLE
            allSemesterText.visibility = INVISIBLE
            rvBestSemester.visibility = INVISIBLE
        } else {
            no_semester_txt.visibility = INVISIBLE
            allSemesterText.visibility = VISIBLE
            bestSemester.visibility = VISIBLE
            semesterList.onEach {
                if (semesterList.size == 1 && it.courses.isEmpty()) {
                    bestSemesterText2.visibility = VISIBLE
                    rvBestSemester.visibility = INVISIBLE
                } else {
                    bestSemesterText2.visibility = INVISIBLE
                    rvBestSemester.visibility = VISIBLE
                }
            }
        }
    }

    private val itemTouchHelperCallback = object : SimpleCallback(
        0, LEFT or RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: ViewHolder,
            target: ViewHolder
        ): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
            val position = viewHolder.layoutPosition
            val semester = semesterAdapter.differ.currentList[position]
            homeViewModel.deleteSemester(semester.id)
            Snackbar.make(
                requireView(), "semester deleted",
                Snackbar.LENGTH_LONG
            ).apply {
                setAction("Undo") {
                    homeViewModel.insertSemester(semester)
                    homeViewModel.deleteLocallyDeletedSemesterId(semester.id)
                }
                show()
            }
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            if (actionState == ACTION_STATE_SWIPE) {
                swipingItem.postValue(isCurrentlyActive)
            }
        }

    }

    private fun setupSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener {
            homeViewModel.syncAllSemesters()
            (activity as MainActivity).handleNetworkChanges()
        }
    }

    private fun datePattern(month: Int): String {
        val first = "MMMM d"
        val last = ", yyyy"
        val position = when (month) {
            1 or 21 or 31 -> "'st'"
            2 or 22 -> "'nd'"
            3 or 23 -> "'rd'"
            else -> "'th'"
        }
        return first + position + last
    }

    private fun getFormattedDate(): String {
        val localDate = Calendar.getInstance()
        val time = Calendar.getInstance().time
        val month = localDate.get(Calendar.DAY_OF_MONTH)
        val formatter = SimpleDateFormat(datePattern(month), Locale.getDefault())
        return formatter.format(time)
    }
}