package com.crushtech.mycollegecgpa.ui.fragments.home

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.View.*
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.crushtech.mycollegecgpa.adapters.BestSemesterAdapter
import com.crushtech.mycollegecgpa.adapters.SemesterAdapter
import com.crushtech.mycollegecgpa.data.local.entities.Semester
import com.crushtech.mycollegecgpa.dialogs.AddOwnerDialogFragment
import com.crushtech.mycollegecgpa.dialogs.AddSemesterDialogFragment
import com.crushtech.mycollegecgpa.dialogs.ItemNotOwnedDialogFragment
import com.crushtech.mycollegecgpa.ui.BaseFragment
import com.crushtech.mycollegecgpa.utils.Constants.IS_LOGGED_IN
import com.crushtech.mycollegecgpa.utils.Constants.KEY_LOGGED_IN_EMAIL
import com.crushtech.mycollegecgpa.utils.Constants.NO_EMAIL
import com.crushtech.mycollegecgpa.utils.Constants.getCurrentUserName
import com.crushtech.mycollegecgpa.utils.Constants.setupDecorator
import com.crushtech.mycollegecgpa.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.home_layout.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

const val ADD_SEMESTER_DIALOG = "add semester dialog"
const val ADD_OWNER_DIALOG = "add owner dialog"
const val NOT_OWNER_DIALOG = "not owner dialog"

@AndroidEntryPoint
class HomeFragment : BaseFragment(R.layout.home_layout) {
    @Inject
    lateinit var sharedPrefs: SharedPreferences

    private lateinit var semesterAdapter: SemesterAdapter
    private lateinit var bestSemesterAdapter: BestSemesterAdapter

    private val homeViewModel: HomeViewModel by viewModels()

    private val swipingItem = MutableLiveData(false)

    private var currentSemester: Semester? = null

    private var authEmail: String? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).showAppBar()

        requireActivity().titleBarText.text = "My Semesters"

        (activity as MainActivity).showMainActivityUI()

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        val username = "Hello, ${getCurrentUserName(sharedPrefs)}"
        userName.text = username
        currentDate.text = getFormattedDate()

        authEmail = sharedPrefs.getString(
            KEY_LOGGED_IN_EMAIL,
            NO_EMAIL
        ) ?: NO_EMAIL

        sharedPrefs.edit().putBoolean(IS_LOGGED_IN, true).apply()

        if (savedInstanceState != null) {
            val addSemesterDialog = parentFragmentManager.findFragmentByTag(ADD_SEMESTER_DIALOG)
                    as AddSemesterDialogFragment?
            addSemesterDialog?.setPositiveListener { semesterName ->
                insertSemester(semesterName)
            }
            val addOwnerDialog = parentFragmentManager.findFragmentByTag(ADD_OWNER_DIALOG)
                    as AddOwnerDialogFragment?
            addOwnerDialog?.apply {
                setPositiveListener { owner, clicked ->
                    addOwnerToSemester(owner)
                    if (clicked) {
                        semesterAdapter.notifyDataSetChanged()
                    }
                }
                setNegativeListener { clicked ->
                    if (clicked) {
                        semesterAdapter.notifyDataSetChanged()
                    }

                }
            }

        }
        viewPerformance.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections
                    .actionHomeFragmentToStatisticsFragment()
            )
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
            //check if semester belongs to the current user
            if (semester.owners[0] == authEmail || semester.owners == listOf(authEmail)) {
                findNavController().navigate(
                    HomeFragmentDirections
                        .actionHomeFragmentToCourseListFragment(
                            semester.id,
                            semester.semesterName
                        )
                )
            } else {
                ItemNotOwnedDialogFragment().apply {
                    val bundle = Bundle()
                    bundle.putString("owner", semester.owners[0])
                    arguments = bundle
                    setPositiveListener { deleteBtnClicked ->
                        if (deleteBtnClicked) {
                            homeViewModel.deleteSemester(semester.id)
                        }
                    }
                }.show(parentFragmentManager, NOT_OWNER_DIALOG)
            }
        }
    }

    private fun setupRecyclerView() = rvAllSemester.apply {
        semesterAdapter = SemesterAdapter(authEmail!!)
        adapter = semesterAdapter
        layoutManager = LinearLayoutManager(requireContext())
        ItemTouchHelper(itemTouchHelperCallback)
            .attachToRecyclerView(this)
    }

    private fun setUpBestSemesterRecyclerView() = rvBestSemester.apply {
        bestSemesterAdapter = BestSemesterAdapter()
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
                        swipeRefreshLayout.isRefreshing = false
                        semesterAdapter.differ.submitList(results.data!!)
                        if (results.data.isNotEmpty()) {
                            bestSemesterAdapter.differ.submitList(
                                listOf(getBestSemester(results.data))
                            )
                        }
                        checkForEmptyState(results.data)
                    }
                    Status.ERROR -> {
                        event.getContentIfNotHandled()?.let { error ->
                            error.message?.let { message ->
                                showSnackbar(
                                    message, null,
                                    R.drawable.ic_baseline_error_outline_24,
                                    "", Color.RED
                                )

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

        homeViewModel.addOwnerStatus.observe(viewLifecycleOwner, Observer { event ->
            val progressBg: LinearLayout = (activity as MainActivity).progressBg
            event?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        addOwnerProgressImage.visibility = GONE
                        progressBg.visibility = GONE
                        addOwnerProgressBar.visibility = GONE

                        showSnackbar(
                            result.message ?: "Successfully shared semester",
                            null,
                            R.drawable.ic_baseline_bubble_chart_24,
                            "", Color.BLACK
                        )

                    }
                    Status.ERROR -> {
                        addOwnerProgressImage.visibility = GONE
                        progressBg.visibility = GONE
                        addOwnerProgressBar.visibility = GONE

                        showSnackbar(
                            result.message ?: "An unknown error occurred", null,
                            R.drawable.ic_baseline_error_outline_24,
                            "", Color.RED
                        )
                    }
                    Status.LOADING -> {
                        addOwnerProgressImage.visibility = VISIBLE
                        progressBg.visibility = VISIBLE
                        addOwnerProgressBar.visibility = VISIBLE
                    }
                }
            }
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

        showSnackbar(
            "semester created", null,
            R.drawable.ic_baseline_bubble_chart_24,
            "", Color.BLACK
        )

    }

    private fun showAddOwnerToSemesterDialog() {
        AddOwnerDialogFragment().apply {
            setPositiveListener { owner, clicked ->
                addOwnerToSemester(owner)
                if (clicked) {
                    semesterAdapter.notifyDataSetChanged()
                }
            }
            setNegativeListener { clicked ->
                if (clicked) {
                    semesterAdapter.notifyDataSetChanged()
                }
            }
        }.show(parentFragmentManager, ADD_OWNER_DIALOG)

    }

    private fun addOwnerToSemester(email: String) {
        currentSemester?.let {
            homeViewModel.addOwnerToSemester(email, it.id)
        }
    }


    private fun checkForEmptyState(semesterList: List<Semester>) {
        if (semesterList.isNullOrEmpty()) {
            bestSemester.visibility = INVISIBLE
            bestSemesterText2.visibility = INVISIBLE
            no_semester_txt.visibility = VISIBLE
            semesterLottie.visibility = VISIBLE
            no_semester_desc.visibility = VISIBLE
            allSemesterText.visibility = INVISIBLE
            viewPerformance.visibility = INVISIBLE
            rvBestSemester.visibility = INVISIBLE
        } else {
            no_semester_txt.visibility = INVISIBLE
            semesterLottie.visibility = INVISIBLE
            no_semester_desc.visibility = INVISIBLE
            allSemesterText.visibility = VISIBLE
            bestSemester.visibility = VISIBLE
            viewPerformance.visibility = VISIBLE

            val allItemsHasNoCourses = semesterList.all {
                it.courses.isNullOrEmpty()
            }
            if (allItemsHasNoCourses) {
                bestSemesterText2.visibility = VISIBLE
                rvBestSemester.visibility = INVISIBLE
            } else {
                bestSemesterText2.visibility = INVISIBLE
                rvBestSemester.visibility = VISIBLE
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
            return false
        }

        override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
            val position = viewHolder.layoutPosition
            val semester = semesterAdapter.differ.currentList[position]
            if (direction == LEFT) {
                homeViewModel.deleteSemester(semester.id)
                semesterAdapter.notifyItemRemoved(position)

                val snackListener = OnClickListener {
                    homeViewModel.insertSemester(semester)
                    homeViewModel.deleteLocallyDeletedSemesterId(semester.id)
                }
                showSnackbar(
                    "semester deleted", snackListener, R.drawable.ic_baseline_delete_24,
                    "Undo", Color.BLACK
                )

            }
            if (direction == RIGHT) {
                homeViewModel.observeSemesterById(semester.id).observe(viewLifecycleOwner,
                    Observer {
                        it?.let { semester ->
                            currentSemester = semester
                        }
                    })
                showAddOwnerToSemesterDialog()
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
            if (actionState == ACTION_STATE_SWIPE) {
                swipingItem.postValue(isCurrentlyActive)
            }
            setupDecorator(
                c, recyclerView, viewHolder,
                dX, dY, actionState,
                isCurrentlyActive
            )

            super.onChildDraw(
                c,
                recyclerView,
                viewHolder,
                dX / 4,
                dY,
                actionState,
                isCurrentlyActive
            )

        }


    }

    private fun setupSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener {
            homeViewModel.syncAllSemesters()
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

    override fun onAttach(context: Context) {
        (activity as MainActivity)
            .window.clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        super.onAttach(context)
    }
}