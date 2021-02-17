package com.crushtech.myccgpa.ui.fragments.weights

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.crushtech.myccgpa.MainActivity
import com.crushtech.myccgpa.R
import com.crushtech.myccgpa.adapters.WeightItemsAdapter
import com.crushtech.myccgpa.data.local.entities.GradeClass
import com.crushtech.myccgpa.data.local.entities.GradeSimplified
import com.crushtech.myccgpa.databinding.WeightLayoutBinding
import com.crushtech.myccgpa.dialogs.EditWeightDialogFragment
import com.crushtech.myccgpa.dialogs.ResetWeightDialogFragment
import com.crushtech.myccgpa.ui.BaseFragment
import com.crushtech.myccgpa.utils.Status
import com.crushtech.myccgpa.utils.viewLifecycle
import dagger.hilt.android.AndroidEntryPoint

const val EDIT_WEIGHT_DIALOG = "edit weight dialog"
const val RESET_WEIGHT_DIALOG = "reset weight dialog"

@AndroidEntryPoint
class WeightFragment : BaseFragment(R.layout.weight_layout) {
    private lateinit var weightItemsAdapter: WeightItemsAdapter
    private val weightViewModel: WeightViewModel by viewModels()
    private var currentGradePoint: GradeClass? = null
    private var binding: WeightLayoutBinding by viewLifecycle()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = WeightLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).apply {
            hideMainActivityUI()
            showAppBar()
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_chevron_left_24)
            activityMainBinding.titleBarText.text = getString(R.string.my_weights)
        }


        setUpRecyclerView()
        setUpObservers()
        syncWeight()

        binding.weightWarningTv.apply {
            ellipsize = TextUtils.TruncateAt.MARQUEE
            isSingleLine = true
            isSelected = true
        }
        if (savedInstanceState != null) {
            val editWeightDialog = parentFragmentManager.findFragmentByTag(EDIT_WEIGHT_DIALOG)
                    as EditWeightDialogFragment?
            editWeightDialog?.setPositiveListener { weightOverflowed, weights ->
                if (weightOverflowed) {
                    showSnackBar(
                        "can't update: weights should be between 0 and 20",
                        null,
                        R.drawable.ic_baseline_error_outline_24,
                        "", Color.BLACK
                    )
                } else {
                    updateWeight(weights)
                }
            }
            val resetWeightDialog = parentFragmentManager.findFragmentByTag(RESET_WEIGHT_DIALOG)
                    as ResetWeightDialogFragment?
            resetWeightDialog?.setPositiveListener { reset ->
                if (reset) {
                    resetWeight(reset)
                }
            }
        }



        weightItemsAdapter.setOnItemClickListener {
            val bundle = Bundle()
            bundle.putSerializable("GradeSimplified", it)
            EditWeightDialogFragment().apply {
                arguments = bundle
                setPositiveListener { weightOverflowed, weights ->
                    if (weightOverflowed) {
                        showSnackBar(
                            "can't update: weights should be between 0 and 20",
                            null,
                            R.drawable.ic_baseline_error_outline_24,
                            "", Color.BLACK
                        )
                    } else {
                        updateWeight(weights)
                    }

                }
            }.show(parentFragmentManager, EDIT_WEIGHT_DIALOG)
        }

        setHasOptionsMenu(true)
    }


    private fun setUpRecyclerView() = binding.weightRecView.apply {
        weightItemsAdapter = WeightItemsAdapter()
        adapter = weightItemsAdapter
        layoutManager = LinearLayoutManager(requireContext())
        addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
    }


    private fun setUpObservers() {
        weightViewModel.allGradePoints.observe(viewLifecycleOwner, Observer {
            val result = it.peekContent()
            when (result.status) {
                Status.SUCCESS -> {
                    binding.weightSwipeRefresh.isRefreshing = false
                    binding.weightPb.visibility = View.GONE
                    val gradePoints = result.data ?: GradeClass()
                    currentGradePoint = gradePoints
                    val gradeList = mutableListOf<GradeSimplified>()
                    gradeList.apply {
                        add(GradeSimplified("A+", gradePoints.APlusGrade))
                        add(GradeSimplified("A-", gradePoints.AMinusGrade))
                        add(GradeSimplified("B+", gradePoints.BPlusGrade))
                        add(GradeSimplified("B", gradePoints.BGrade))
                        add(GradeSimplified("B-", gradePoints.BMinusGrade))
                        add(GradeSimplified("C+", gradePoints.CPlusGrade))
                        add(GradeSimplified("C", gradePoints.CGrade))
                        add(GradeSimplified("C-", gradePoints.CMinusGrade))
                        add(GradeSimplified("D+", gradePoints.DPlusGrade))
                        add(GradeSimplified("D", gradePoints.DGrade))
                        add(GradeSimplified("E/F", gradePoints.FOrEGrade))
                    }
                    weightItemsAdapter.differ.submitList(gradeList)
                }
                Status.ERROR -> {
                    binding.weightSwipeRefresh.isRefreshing = false
                    binding.weightPb.visibility = View.GONE
                    showSnackBar(
                        result.message ?: "an unknown error occurred",
                        null,
                        R.drawable.ic_baseline_error_outline_24,
                        "", Color.RED
                    )
                }
                Status.LOADING -> {
                    binding.weightSwipeRefresh.isRefreshing = true
                    binding.weightPb.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun updateWeight(gradeSimplified: GradeSimplified) {
        currentGradePoint?.let { gradePoint ->
            when (gradeSimplified.name) {
                "A+" -> {
                    gradePoint.APlusGrade = gradeSimplified.gradePoint
                }
                "A-" -> {
                    gradePoint.AMinusGrade = gradeSimplified.gradePoint
                }
                "B+" -> {
                    gradePoint.BPlusGrade = gradeSimplified.gradePoint
                }
                "B" -> {
                    gradePoint.BGrade = gradeSimplified.gradePoint
                }
                "B-" -> {
                    gradePoint.BMinusGrade = gradeSimplified.gradePoint

                }
                "C+" -> {
                    gradePoint.CPlusGrade = gradeSimplified.gradePoint

                }
                "C" -> {
                    gradePoint.CGrade = gradeSimplified.gradePoint

                }
                "C-" -> {
                    gradePoint.CMinusGrade = gradeSimplified.gradePoint

                }
                "D+" -> {
                    gradePoint.DPlusGrade = gradeSimplified.gradePoint

                }
                "D" -> {
                    gradePoint.DGrade = gradeSimplified.gradePoint

                }
                else -> {
                    // it's E/F
                    gradePoint.FOrEGrade = gradeSimplified.gradePoint
                }
            }
            weightViewModel.insertGradesPoints(gradePoint)
            showSnackBar(
                "weights updated",
                null,
                R.drawable.ic_baseline_bubble_chart_24,
                "", Color.BLACK
            )
        }

    }

    private fun syncWeight() {
        binding.weightSwipeRefresh.setOnRefreshListener {
            weightViewModel.syncGradePoints()
            showSnackBar(
                "all caught up",
                null,
                R.drawable.ic_baseline_bubble_chart_24,
                "", Color.BLACK
            )
        }
    }

    private fun resetWeight(resetState: Boolean) {
        if (resetState) {
            weightViewModel.resetGradePoints()
        }
        currentGradePoint?.let {
            if (it == GradeClass(id = it.id)) {
                showSnackBar(
                    "no changes detected",
                    null,
                    R.drawable.ic_baseline_emoji_emotions_24,
                    "", Color.BLACK
                )
            } else {
                showSnackBar(
                    "your weights has been set to default",
                    null,
                    R.drawable.ic_baseline_bubble_chart_24,
                    "", Color.BLACK
                )
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.resetWeight -> {
                ResetWeightDialogFragment().apply {
                    setPositiveListener { reset ->
                        if (reset) {
                            resetWeight(reset)
                        }
                    }
                }.show(parentFragmentManager, RESET_WEIGHT_DIALOG)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.reset_weight_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onDestroy() {
        if (this::weightItemsAdapter.isInitialized) {
            weightItemsAdapter.binding = null
        }
        super.onDestroy()
    }
}



