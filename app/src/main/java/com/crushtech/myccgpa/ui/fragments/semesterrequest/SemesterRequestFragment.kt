package com.crushtech.myccgpa.ui.fragments.semesterrequest

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.crushtech.myccgpa.MainActivity
import com.crushtech.myccgpa.R
import com.crushtech.myccgpa.adapters.FilterItems
import com.crushtech.myccgpa.adapters.SemFilterItemAdapter
import com.crushtech.myccgpa.adapters.SemesterRequestItemsAdapter
import com.crushtech.myccgpa.data.local.entities.STATE
import com.crushtech.myccgpa.data.local.entities.SemesterRequests
import com.crushtech.myccgpa.databinding.SemReqOptLayoutBinding
import com.crushtech.myccgpa.databinding.SemesterFilterBsheetBinding
import com.crushtech.myccgpa.databinding.SemesterRequestLayoutBinding
import com.crushtech.myccgpa.ui.BaseFragment
import com.crushtech.myccgpa.utils.Status
import com.crushtech.myccgpa.utils.viewLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SemesterRequestFragment : BaseFragment(R.layout.semester_request_layout) {
    private var binding: SemesterRequestLayoutBinding by viewLifecycle()

    private lateinit var semesterRequestItemsAdapter: SemesterRequestItemsAdapter
    private lateinit var semFilterAdapter: SemFilterItemAdapter

    private var filterItemsList: ArrayList<FilterItems>? = null
    private val semesterRequestViewModel: SemesterRequestViewModel by viewModels()

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SemesterRequestLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).apply {
            hideMainActivityUI()
            showAppBar()
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_chevron_left_24)
            activityMainBinding.titleBarText.text = getString(R.string.semester_requests)
        }
        setUpRecyclerview()
        setupSwipeRefreshLayout()
        subscribeToObservers()
        setUpBottomSheet()
        setHasOptionsMenu(true)
        showSemesterOptionsRequest()

    }

    private fun setUpRecyclerview() {
        // initFakeData()
        binding.semReqRv.apply {
            itemAnimator = null
            itemAnimator?.changeDuration = 0
            semesterRequestItemsAdapter = SemesterRequestItemsAdapter()
            adapter = semesterRequestItemsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun subscribeToObservers() {
        semesterRequestViewModel.allSemestersRequests.observe(viewLifecycleOwner, {
            it.peekContent().let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        binding.swipeRefreshLayout.isRefreshing = false
                        result.data?.let { semList ->
                            semesterRequestItemsAdapter.differ.submitList(semList)
                            handleEmptyState(semList)
                        }
                    }
                    Status.ERROR -> {
                        binding.swipeRefreshLayout.isRefreshing = false
                        it.getContentIfNotHandled()?.let { error ->
                            error.message?.let { message ->
                                showSnackBar(
                                    message, null,
                                    R.drawable.ic_baseline_error_outline_24,
                                    "", Color.RED
                                )

                            }
                        }
                        result.data?.let { semList ->
                            semesterRequestItemsAdapter.differ.submitList(semList)
                            handleEmptyState(semList)
                        }
                    }
                    Status.LOADING -> {
                        binding.swipeRefreshLayout.isRefreshing = true
                        showSnackBar(
                            "Loading please wait",
                            null,
                            R.drawable.ic_baseline_bubble_chart_24,
                            "", Color.BLACK
                        )
                        result.data?.let { semList ->
                            semesterRequestItemsAdapter.differ.submitList(semList)
                            handleEmptyState(semList)
                        }
                    }
                }
            }
        })
    }


    private fun showSemesterOptionsRequest() {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.AppTheme)
        val requestBinding = SemReqOptLayoutBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(requestBinding.root)
        semesterRequestItemsAdapter.setOnItemClickListener { semesterRequest ->
            requestBinding.apply {
                cancelProcess.setOnClickListener {
                    showSnackBar(
                        "operation cancelled", null,
                        R.drawable.ic_clear4, "", Color.BLACK
                    )
                    bottomSheetDialog.cancel()
                }
                acceptSem.setOnClickListener {
                    if (semesterRequest.state != STATE.ACCEPTED
                        || semesterRequest.state == STATE.REJECTED
                    ) {
                        semesterRequestViewModel.acceptSharedSemester(semesterRequest)
                        bottomSheetDialog.cancel()
                        showSnackBar(
                            "semester request accepted", null,
                            R.drawable.ic_baseline_check_24, "",
                            Color.BLACK
                        )
                    } else {
                        bottomSheetDialog.cancel()
                        showSnackBar(
                            "semester request was already accepted or rejected",
                            null,
                            R.drawable.ic_baseline_error_outline_24, "",
                            Color.BLACK
                        )
                    }
                }
                rejectSem.setOnClickListener {
                    if (semesterRequest.state != STATE.ACCEPTED) {
                        semesterRequestViewModel.rejectSharedSemester(semesterRequest)
                        bottomSheetDialog.cancel()
                        showSnackBar(
                            "semester rejected", null,
                            R.drawable.ic_clear4, "", Color.BLACK
                        )
                    } else {
                        bottomSheetDialog.cancel()
                        showSnackBar(
                            "can't reject, semester request was already accepted",
                            null,
                            R.drawable.ic_baseline_error_outline_24, "",
                            Color.BLACK
                        )
                    }
                }
                deleteSem.setOnClickListener {
                    semesterRequestViewModel.deleteSemRequest(semesterRequest.id)
                    bottomSheetDialog.cancel()
                    showSnackBar(
                        "semester request deleted", null,
                        R.drawable.ic_baseline_delete_24,
                        "", Color.BLACK
                    )
                }
            }
            openBottomSheet(bottomSheetDialog)
        }
    }

    private fun setUpBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.AppTheme)
        val semesterFilterBinding =
            SemesterFilterBsheetBinding.inflate(layoutInflater)
        setUpBottomSheetRecyclerView(semesterFilterBinding)
        bottomSheetDialog.setContentView(semesterFilterBinding.root)

        binding.filterIv.setOnClickListener {
            openBottomSheet(bottomSheetDialog)
        }

        semFilterAdapter.setOnItemClickListener { position ->
            bottomSheetDialog.cancel()
            when (position) {
                0 -> {
                    semesterRequestItemsAdapter.differ.submitList(listOf())
                    semesterRequestViewModel.allSemestersRequests.observe(viewLifecycleOwner, {
                        it.peekContent().data?.let { result ->
                            semesterRequestItemsAdapter.differ.submitList(result)
                            handleEmptyState(result)
                        }
                    })
                }
                1 -> {
                    // semesterRequestItemsAdapter.differ.submitList(listOf())
                    semesterRequestViewModel.getAcceptedSemList()
                        .observe(viewLifecycleOwner, {
                            semesterRequestItemsAdapter.differ.currentList.filter { semReq ->
                                semReq.state != STATE.ACCEPTED
                            }
                            semesterRequestItemsAdapter.differ.submitList(it)
                            handleEmptyState(it)
                        })
                }
                2 -> {
                    // semesterRequestItemsAdapter.differ.submitList(listOf())
                    semesterRequestViewModel.getRejectedSemList()
                        .observe(viewLifecycleOwner, {
                            semesterRequestItemsAdapter.differ.currentList.filter { semReq ->
                                semReq.state != STATE.REJECTED
                            }
                            semesterRequestItemsAdapter.differ.submitList(it)
                            handleEmptyState(it)
                        })
                }
                3 -> {
                    semesterRequestViewModel.getPendingSemList()
                        .observe(viewLifecycleOwner, {
                            semesterRequestItemsAdapter.differ.currentList.filter { semReq ->
                                semReq.state != STATE.REJECTED
                            }
                            semesterRequestItemsAdapter.differ.submitList(it)
                            handleEmptyState(it)
                        })
                }
            }
        }
    }

    private fun setupSwipeRefreshLayout() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            semesterRequestViewModel.syncAllSemestersRequest()
        }
    }

    private fun setUpBottomSheetRecyclerView(binding: SemesterFilterBsheetBinding) {
        setUpBottomSheetItems()
        semFilterAdapter = SemFilterItemAdapter()
        binding.rvBottomSheet.apply {
            adapter = semFilterAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                ).also {
                    val divider = ContextCompat.getDrawable(
                        requireContext(), R.drawable.extra_items_divider
                    )
                    divider?.let { drawable ->
                        it.setDrawable(drawable)
                    }
                }
            )
        }
        semFilterAdapter.differ.submitList(filterItemsList!!)
    }

    private fun setUpBottomSheetItems() {
        filterItemsList = ArrayList()
        filterItemsList!!.apply {
            add(FilterItems("None"))
            add(FilterItems("Accepted"))
            add(FilterItems("Rejected"))
            add(FilterItems("Pending"))
        }
    }

    private fun openBottomSheet(bottomSheetDialog: BottomSheetDialog) {
        if (bottomSheetDialog.window != null) {
            bottomSheetDialog.window!!
                .setBackgroundDrawableResource(android.R.color.transparent)
        }
        bottomSheetDialog.show()
    }

    private fun handleEmptyState(semList: List<SemesterRequests>) {
        binding.noSemReq.isVisible = semList.isNullOrEmpty()
        binding.noSemReqDes.isVisible = semList.isNullOrEmpty()
        binding.lottieAnimationView.isVisible = semList.isNullOrEmpty()
    }

    override fun onDestroy() {
        semFilterAdapter.binding = null
        semesterRequestItemsAdapter.binding = null
        super.onDestroy()
    }
}