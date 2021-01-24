package com.crushtech.myccga.ui.fragments.home


import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.isEmpty
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.crushtech.myccga.MainActivity
import com.crushtech.myccga.R
import com.crushtech.myccga.adapters.BestSemesterAdapter
import com.crushtech.myccga.adapters.SemesterAdapter
import com.crushtech.myccga.data.local.entities.Semester
import com.crushtech.myccga.databinding.HomeLayoutBinding
import com.crushtech.myccga.dialogs.AddOwnerDialogFragment
import com.crushtech.myccga.dialogs.AddSemesterDialogFragment
import com.crushtech.myccga.dialogs.ItemNotOwnedDialogFragment
import com.crushtech.myccga.ui.BaseFragment
import com.crushtech.myccga.utils.Constants.IS_LOGGED_IN
import com.crushtech.myccga.utils.Constants.KEY_LOGGED_IN_EMAIL
import com.crushtech.myccga.utils.Constants.NO_EMAIL
import com.crushtech.myccga.utils.Constants.customRecyclerViewScrollListener
import com.crushtech.myccga.utils.Constants.getCurrentUserName
import com.crushtech.myccga.utils.Constants.setupDecorator
import com.crushtech.myccga.utils.Status
import com.crushtech.myccga.utils.viewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

const val ADD_SEMESTER_DIALOG = "add semester dialog"
const val ADD_OWNER_DIALOG = "add owner dialog"
const val NOT_OWNER_DIALOG = "not owner dialog"

@AndroidEntryPoint
class HomeFragment : BaseFragment(R.layout.home_layout) {
    private var binding: HomeLayoutBinding by viewLifecycle()

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    private lateinit var semesterAdapter: SemesterAdapter
    private lateinit var bestSemesterAdapter: BestSemesterAdapter

    private val homeViewModel: HomeViewModel by viewModels()

    private val swipingItem = MutableLiveData(false)

    private var currentSemester: Semester? = null

    private var authEmail: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HomeLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).apply {
            showAppBar()
            activityMainBinding.titleBarText.text = getString(R.string.mysemesters)
            showMainActivityUI()
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        }


        val username = "Hello, ${getCurrentUserName(sharedPrefs)}"
        binding.userName.text = username
        binding.currentDate.text = getFormattedDate()

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
        binding.viewPerformance.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections
                    .actionHomeFragmentToStatisticsFragment()
            )
        }
        setupRecyclerView()
        setUpBestSemesterRecyclerView()
        setupSwipeRefreshLayout()
        subscribeToObservers()


        binding.addSemesterFab.setOnClickListener {
            showCreateSemesterDialog()
        }

        val cancelIcon = binding.semesterSearch.findViewById<ImageView>(R.id.search_close_btn)
        cancelIcon.setColorFilter(R.color.colorPrimary)
        cancelIcon.setOnClickListener {
            //clear and reload items
            binding.semesterSearch.setQuery("", false)
            semesterAdapter.notifyDataSetChanged()
        }

        binding.semesterSearch.setOnQueryTextListener(object : OnQueryTextListener {
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
                    setDeleteCourseListener { deleteBtnClicked ->
                        if (deleteBtnClicked) {
                            homeViewModel.deleteSemester(semester.id)
                            showSnackBar(
                                "semester deleted", null,
                                R.drawable.ic_baseline_bubble_chart_24,
                                "", Color.BLACK
                            )
                        }
                    }
                    setProceedListener {
                        findNavController().navigate(
                            HomeFragmentDirections
                                .actionHomeFragmentToCourseListFragment(
                                    semester.id,
                                    semester.semesterName
                                )
                        )
                    }
                }.show(parentFragmentManager, NOT_OWNER_DIALOG)
            }
        }
    }

    private fun setupRecyclerView() = binding.rvAllSemester.apply {
        semesterAdapter = SemesterAdapter(authEmail!!)
        adapter = semesterAdapter
        layoutManager = LinearLayoutManager(requireContext())
        ItemTouchHelper(itemTouchHelperCallback)
            .attachToRecyclerView(this)
        itemAnimator?.changeDuration = 0
        addOnScrollListener(customRecyclerViewScrollListener(listOf(binding.addSemesterFab)))
    }


    private fun setUpBestSemesterRecyclerView() = binding.rvBestSemester.apply {
        bestSemesterAdapter = BestSemesterAdapter()
        adapter = bestSemesterAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun getBestSemester(semester: List<Semester>): Semester? {
        return semester.maxByOrNull {
            it.getGPA()
        }
    }

    private fun subscribeToObservers() {
        homeViewModel.allSemesters.observe(viewLifecycleOwner, {
            it?.let { event ->
                val results = event.peekContent()
                when (results.status) {
                    Status.SUCCESS -> {
                        binding.swipeRefreshLayout.isRefreshing = false
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
                                showSnackBar(
                                    message, null,
                                    R.drawable.ic_baseline_error_outline_24,
                                    "", Color.RED
                                )

                            }
                        }
                        results.data?.let { semesters ->
                            semesterAdapter.differ.submitList(semesters)
                            if (semesters.isNotEmpty()) {
                                bestSemesterAdapter.differ.submitList(
                                    listOf(getBestSemester(semesters))
                                )
                            }
                            checkForEmptyState(semesters)
                        }
                        binding.swipeRefreshLayout.isRefreshing = false
                    }
                    Status.LOADING -> {
                        results.data?.let { semesters ->
                            semesterAdapter.differ.submitList(semesters)
                            if (semesters.isNotEmpty()) {
                                bestSemesterAdapter.differ.submitList(
                                    listOf(getBestSemester(semesters))
                                )
                            }
                            checkForEmptyState(semesters)
                        }
                        binding.swipeRefreshLayout.isRefreshing = true
                    }
                }
            }
        })
        swipingItem.observe(viewLifecycleOwner, {
            binding.swipeRefreshLayout.isEnabled = !it
        })


        homeViewModel.addOwnerStatus.observe(viewLifecycleOwner, { event ->
            event?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        binding.addOwnerProgressImage.visibility = GONE
                        binding.addOwnerProgressBar.visibility = GONE
                        (activity as MainActivity)
                            .activityMainBinding.mainActivityTransBg.visibility = View.GONE
                        showSnackBar(
                            result.message ?: "Successfully shared semester",
                            null,
                            R.drawable.ic_baseline_bubble_chart_24,
                            "", Color.BLACK
                        )

                    }
                    Status.ERROR -> {
                        binding.addOwnerProgressImage.visibility = GONE
                        binding.addOwnerProgressBar.visibility = GONE
                        (activity as MainActivity)
                            .activityMainBinding.mainActivityTransBg.visibility = View.GONE
                        showSnackBar(
                            result.message ?: "An unknown error occurred", null,
                            R.drawable.ic_baseline_error_outline_24,
                            "", Color.RED
                        )
                    }
                    Status.LOADING -> {
                        (activity as MainActivity)
                            .activityMainBinding.mainActivityTransBg.visibility = View.VISIBLE
                        binding.addOwnerProgressImage.visibility = VISIBLE
                        binding.addOwnerProgressBar.visibility = VISIBLE
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

        showSnackBar(
            "semester created", null,
            R.drawable.ic_baseline_bubble_chart_24,
            "", Color.BLACK
        )
        setupSwipeRefreshLayout()
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
            binding.bestSemester.visibility = INVISIBLE
            binding.bestSemesterText2.visibility = INVISIBLE
            binding.noSemesterTxt.visibility = VISIBLE
            binding.semesterLottie.visibility = VISIBLE
            binding.noSemesterDesc.visibility = VISIBLE
            binding.allSemesterText.visibility = INVISIBLE
            binding.viewPerformance.visibility = INVISIBLE
            binding.rvBestSemester.visibility = INVISIBLE
            (activity as MainActivity).showMainActivityUI()
        } else {
            (activity as MainActivity).showMainActivityUI()
            binding.noSemesterTxt.visibility = INVISIBLE
            binding.semesterLottie.visibility = INVISIBLE
            binding.noSemesterDesc.visibility = INVISIBLE
            binding.allSemesterText.visibility = VISIBLE
            binding.bestSemester.visibility = VISIBLE
            binding.viewPerformance.visibility = VISIBLE

            val allItemsHasNoCourses = semesterList.all {
                it.courses.isNullOrEmpty()
            }
            if (allItemsHasNoCourses) {
                binding.bestSemesterText2.visibility = VISIBLE
                binding.rvBestSemester.visibility = INVISIBLE
            } else {
                binding.bestSemesterText2.visibility = INVISIBLE
                binding.rvBestSemester.visibility = VISIBLE
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
                try {
                    homeViewModel.deleteSemester(semester.id)
                } catch (e: Exception) {
                }
                semesterAdapter.notifyItemRemoved(position)

                val snackListener = OnClickListener {
                    homeViewModel.insertSemester(semester)
                    homeViewModel.deleteLocallyDeletedSemesterId(semester.id)
                }
                showSnackBar(
                    "semester deleted", snackListener, R.drawable.ic_baseline_delete_24,
                    "Undo", Color.BLACK
                )

            }
            if (direction == RIGHT) {
                homeViewModel.observeSemesterById(semester.id).observe(viewLifecycleOwner,
                    {
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
        binding.swipeRefreshLayout.setOnRefreshListener {

            //clear searchview on refresh
            if (!(binding.semesterSearch.isEmpty())) {
                binding.semesterSearch.setQuery("", false)
            }
            semesterAdapter.notifyDataSetChanged()
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

    override fun onDestroy() {
        if (this::semesterAdapter.isInitialized) {
            semesterAdapter.binding = null
        }
        super.onDestroy()
    }
}













