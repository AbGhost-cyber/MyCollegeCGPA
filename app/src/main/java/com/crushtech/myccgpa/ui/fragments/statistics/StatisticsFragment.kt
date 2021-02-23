package com.crushtech.myccgpa.ui.fragments.statistics

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.crushtech.myccgpa.MainActivity
import com.crushtech.myccgpa.R
import com.crushtech.myccgpa.data.local.entities.UserPdfDownloads
import com.crushtech.myccgpa.databinding.StatisticsFragmentBinding
import com.crushtech.myccgpa.ui.BaseFragment
import com.crushtech.myccgpa.ui.fragments.home.HomeViewModel
import com.crushtech.myccgpa.utils.*
import com.crushtech.myccgpa.utils.Constants.STATISTICS_FIRST_TIME_OPEN
import com.crushtech.myccgpa.utils.Constants.TOTAL_NUMBER_OF_COURSES
import com.crushtech.myccgpa.utils.Constants.TOTAL_NUMBER_OF_CREDIT_HOURS
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


const val REQUEST_CODE = 1

@AndroidEntryPoint
class StatisticsFragment : BaseFragment(R.layout.statistics_fragment), PurchasesUpdatedListener {
    @Inject
    lateinit var sharedPrefs: SharedPreferences
    private var firsTimeOpen = false
    private var pdfHasBeenCreated = false
    private val viewModel: HomeViewModel by viewModels()
    private val pdfDownloadsViewModel: StatisticsViewModel by viewModels()
    private val totalCourseChange: MutableLiveData<Int> = MutableLiveData()
    private val totalCreditHoursChange: MutableLiveData<Float> = MutableLiveData()
    private lateinit var billingClient: BillingClient
    private var userPdfDownloadsCount: UserPdfDownloads? = null
    private val skuList = listOf("stats_download_coins")
    private var binding: StatisticsFragmentBinding by viewLifecycle()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = StatisticsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (!(sharedPrefs.contains(STATISTICS_FIRST_TIME_OPEN))) {
            firsTimeOpen = true
            sharedPrefs.edit().putBoolean(STATISTICS_FIRST_TIME_OPEN, true).apply()

        }
        super.onActivityCreated(savedInstanceState)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pdfDownloadsViewModel.getUserPdfDownloads()
        (activity as MainActivity).apply {
            showAppBar()
            hideMainActivityUI()
            activityMainBinding.titleBarText.text = getString(R.string.my_stats)
            activityMainBinding.mainLayoutToolbar.setNavigationIcon(R.drawable.ic_baseline_chevron_left_24)
        }
        setUpObservers()
        setUpBarCharts()
        setupSwipeRefreshLayout()

        val totalCH = sharedPrefs
            .getFloat(TOTAL_NUMBER_OF_CREDIT_HOURS, 0F)
        val totalCC = sharedPrefs
            .getInt(TOTAL_NUMBER_OF_COURSES, 0)

        totalCourseChange.observe(viewLifecycleOwner, {
            when {
                it == totalCC -> {
                    binding.courseChange.text = "0"
                }
                it > totalCC -> {

                    val result = it - totalCC
                    binding.courseChange.text = result.toString()
                }
                else -> {
                    val result = it - totalCC
                    binding.courseChange.text = result.toString()
                }
            }
            if (firsTimeOpen) {
                sharedPrefs
                    .edit()
                    .putInt(TOTAL_NUMBER_OF_COURSES, it).apply()
            } else {
                sharedPrefs
                    .edit()
                    .putInt(TOTAL_NUMBER_OF_COURSES, totalCC).apply()
            }


            if (binding.courseChange.text.contains("-")) {
                binding.courseChange.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_round_arrow_drop_down_24, 0, 0, 0
                )
                binding.courseChange.setTextColor(
                    getColor(
                        requireContext(),
                        android.R.color.holo_red_dark
                    )
                )
            } else {
                binding.courseChange.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_baseline_arrow_drop_up_24, 0, 0, 0
                )
                binding.courseChange.setTextColor(
                    getColor(
                        requireContext(),
                        R.color.progress_color
                    )
                )
            }
        })
        totalCreditHoursChange.observe(viewLifecycleOwner, {
            when {
                it == totalCH -> {
                    binding.creditHoursChange.text = "0"
                }
                it > totalCH -> {
                    val result = it - totalCH
                    binding.creditHoursChange.text = result.toString()
                }
                totalCH > it -> {
                    val result = it - totalCH
                    binding.creditHoursChange.text = result.toString()
                }
            }
            if (firsTimeOpen) {
                sharedPrefs
                    .edit()
                    .putFloat(TOTAL_NUMBER_OF_CREDIT_HOURS, it).apply()
            } else {
                sharedPrefs
                    .edit()
                    .putFloat(TOTAL_NUMBER_OF_CREDIT_HOURS, totalCH).apply()
            }



            if (binding.creditHoursChange.text.contains("-")) {
                binding.creditHoursChange.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_round_arrow_drop_down_24, 0, 0, 0
                )
                binding.creditHoursChange.setTextColor(
                    getColor(
                        requireContext(),
                        android.R.color.holo_red_dark
                    )
                )
            } else {
                binding.creditHoursChange.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_baseline_arrow_drop_up_24, 0, 0, 0
                )
                binding.creditHoursChange.setTextColor(
                    getColor(
                        requireContext(),
                        R.color.progress_color
                    )
                )
            }
        })
        NetworkUtils.getNetworkLiveData(requireContext())
            .observe(viewLifecycleOwner, { content ->
                val isConnected = content.peekContent()
                if (isConnected) {
                    pdfDownloadsViewModel.getUserPdfDownloads()
                }
                binding.saveAsPdf.setOnClickListener {
                    if (isConnected) {
                        if (binding.viewPdf.isVisible) {
                            binding.viewPdf.visibility = View.GONE
                        }
                        userPdfDownloadsCount?.let { downloads ->
                            if (downloads.noOfPdfDownloads <= 0) {
                                showSnackBar(
                                    "you've ran out of download points, please purchase", null,
                                    R.drawable.ic_baseline_error_outline_24,
                                    "", Color.RED
                                )
                            } else {
                                checkWritePermissionAndProcessPdf()
                            }
                        }
                    } else {
                        showSnackBar(
                            "internet connection is required for this feature",
                            null,
                            R.drawable.ic_baseline_error_outline_24,
                            "", Color.RED
                        )
                    }

                }

            })


    }

    private fun setUpObservers() {
        viewModel.allSemesters.observe(viewLifecycleOwner, {
            it?.let { content ->
                val result = content.peekContent()
                val semester = result.data
                var totalNumberOfCourses = 0
                var totalNumberOfCreditHours = 0F
                semester?.let { semesters ->
                    semesters.forEach { sem ->
                        totalNumberOfCourses += sem.courses.size
                        val courseString = if (totalNumberOfCourses <= 1) {
                            "$totalNumberOfCourses course"
                        } else {
                            "$totalNumberOfCourses courses"
                        }
                        binding.totalCoursesOffered.text = courseString
                        sem.courses.forEach { course ->
                            totalNumberOfCreditHours += course.creditHours
                            val creditHoursString = if (totalNumberOfCreditHours <= 1f) {
                                "$totalNumberOfCreditHours hour"
                            } else {
                                "$totalNumberOfCreditHours hours"
                            }
                            binding.tvCHours.text = creditHoursString
                        }

                        totalCourseChange.postValue(totalNumberOfCourses)
                        totalCreditHoursChange.postValue(totalNumberOfCreditHours)
                        sharedPrefs
                            .edit()
                            .putFloat(TOTAL_NUMBER_OF_CREDIT_HOURS, totalNumberOfCreditHours)
                            .putInt(TOTAL_NUMBER_OF_COURSES, totalNumberOfCourses).apply()

                    }
                    val allGPA =
                        semesters.indices.map { i ->
                            BarEntry(i.toFloat(), semesters[i].getGPA().toFloat())
                        }

                    val semesterCourseList = ArrayList<String>()
                    semester.map { sem ->
                        if (sem.courses.size <= 1) {
                            semesterCourseList.add("${sem.courses.size} course")
                        } else {
                            semesterCourseList.add("${sem.courses.size} courses")
                        }
                    }
                    binding.barChart.xAxis.valueFormatter =
                        IndexAxisValueFormatter(semesterCourseList)

                    val typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.product_sans_regular)


                    val colorList = listOf(
                        getColor(requireContext(), R.color.colorPrimary),
                        getColor(requireContext(), R.color.colorAccent)
                    )

                    val barDataSet = BarDataSet(
                        allGPA,
                        "GPA over courses"
                    ).apply {
                        valueTextColor = Color.BLACK
                        valueTypeface = typeface
                        valueTextSize = 13f
                        colors = colorList
                    }

                    binding.barChart.data = BarData(barDataSet)
                    binding.barChart.invalidate()
                    binding.barChart.marker =
                        CustomMarkerView(semesters, requireContext(), R.layout.marker_view)
                    binding.barChart.description.isEnabled = false
                }
            }

        })

        pdfDownloadsViewModel.pdfDownloads.observe(viewLifecycleOwner, { results ->
            when (results.status) {
                Status.SUCCESS -> {
                    val downloads = results.data!!
                    binding.pdfDownloadCounts.text = downloads.noOfPdfDownloads.toString()
                    userPdfDownloadsCount = downloads
                    binding.statsRefreshLayout.isRefreshing = false
                    pdfDownloadsViewModel.getUserPdfDownloads()

                }
                Status.ERROR -> {
                    showSnackBar(
                        results.message ?: "an error occurred", null,
                        R.drawable.ic_baseline_error_outline_24,
                        "", Color.RED
                    )
                }
                Status.LOADING -> {
                    binding.statsRefreshLayout.isRefreshing = false
                }
            }
        })


    }


    private fun setUpBarCharts() {
        binding.barChart.xAxis.apply {
            position = XAxisPosition.BOTTOM
            axisLineColor = Color.BLACK
            textColor = Color.BLACK
            setDrawGridLines(false)
            granularity = 1f
            isGranularityEnabled = true
            setDrawLabels(true)
        }
        binding.barChart.axisLeft.apply {
            axisLineColor = Color.BLACK
            textColor = Color.BLACK
            setDrawGridLines(false)
        }
        binding.barChart.axisRight.apply {
            axisLineColor = Color.BLACK
            textColor = Color.BLACK
            setDrawGridLines(false)

        }

    }


    private fun processPdf() {
        val pageInfo = PdfDocument.PageInfo.Builder(2250, 1500, 1).create()
        val document = PdfDocument()
        val page = document.startPage(pageInfo)
        val content =
            this.requireActivity().window.decorView.findViewById<ConstraintLayout>(R.id.stats_parent)

        content.measure(2480, 3508)
        content.layout(0, 0, 2480, 3508)

        val measureWidth = View.MeasureSpec.makeMeasureSpec(
            page.canvas.width,
            View.MeasureSpec.EXACTLY
        )
        val measuredHeight = View.MeasureSpec.makeMeasureSpec(
            page.canvas.height,
            View.MeasureSpec.EXACTLY
        )

        content.measure(measureWidth, measuredHeight)
        content.layout(0, 0, page.canvas.width, page.canvas.height)
        binding.apply {
            tCHParent.setCardBackgroundColor(getColor(requireContext(), R.color.colorPrimary))
            tCCParent.setCardBackgroundColor(getColor(requireContext(), R.color.colorPrimary))
            saveAsPdf.visibility = View.INVISIBLE
            pdfDownloadParent.visibility = View.GONE
            sponsored.visibility = View.VISIBLE
            val username = " For ${Constants.getCurrentUserName(sharedPrefs)}"
            userName.text = username
            userName.visibility = View.VISIBLE
        }
        (activity as MainActivity)
            .activityMainBinding.mainActivityTransBg.visibility = View.VISIBLE

        //if barchart markerview was visible, then hide it while creating the pdf
        try {
            binding.barChart.highlightValue(null)
        } catch (e: Exception) {
        }

        content.draw(page.canvas)

        document.finishPage(page)
        val pdfFolder = File(
            requireContext().externalCacheDir?.absolutePath,
            "MY COLLEGE CGPA"
        )

        if (!pdfFolder.exists()) {
            pdfFolder.mkdirs()
            Timber.i("Pdf Directory created")
        }

        //Create time stamp
        val date = Date()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(date)
        val fileNamePath = "$pdfFolder$timeStamp.pdf"
        val myFile = File(fileNamePath)

        val output: OutputStream = FileOutputStream(myFile)

        GlobalScope.launch(Dispatchers.Main) {
            binding.pdfPb.apply {
                progressMax = 100F
                visibility = View.VISIBLE
                indeterminateMode = true
            }

            delay(300L)
            withContext(Dispatchers.IO) {
                delay(200L)
                pdfHasBeenCreated = try {
                    document.writeTo(output)
                    true
                } catch (e: IOException) {
                    false
                }

            }
            binding.pdfPb.visibility = View.GONE
            if (pdfHasBeenCreated) {
                userPdfDownloadsCount?.let {
                    it.noOfPdfDownloads--
                    pdfDownloadsViewModel.upsertUserPdfDownloads(it)
                    pdfHasBeenCreated = false
                }
                showSnackBar(
                    "Pdf created: please check ${myFile.parent} ", null,
                    R.drawable.ic_baseline_bubble_chart_24,
                    "", Color.BLACK, Snackbar.LENGTH_SHORT
                )
                binding.viewPdf.visibility = View.VISIBLE

                binding.viewPdf.setOnClickListener {
                    checkReadPermissionsAndOpenPdf(fileNamePath)
                }
                val countDown = 6000L
                val countDownTimer = object : CountDownTimer(countDown, 1000) {
                    override fun onFinish() {
                        try {
                            binding.viewPdf.visibility = View.GONE
                        } catch (e: Exception) {
                        }
                    }

                    override fun onTick(p0: Long) {
                        val text = "View Pdf ${p0 / 1000}"
                        try {
                            binding.viewPdf.text = text
                        } catch (e: Exception) {
                        }
                    }

                }
                if (binding.viewPdf.visibility == View.VISIBLE) {
                    countDownTimer.start()
                }

                binding.saveAsPdf.visibility = View.VISIBLE
                binding.pdfDownloadParent.visibility = View.VISIBLE
                content.updateLayoutParams {
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
                binding.sponsored.visibility = View.INVISIBLE
                binding.userName.visibility = View.INVISIBLE
                (activity as MainActivity)
                    .activityMainBinding.mainActivityTransBg.visibility = View.GONE
            } else {
                showSnackBar(
                    "an unknown error occurred, please try again", null,
                    R.drawable.ic_baseline_error_outline_24, "", Color.RED
                )
                binding.saveAsPdf.visibility = View.VISIBLE
                content.updateLayoutParams {
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
                binding.sponsored.visibility = View.INVISIBLE
                binding.userName.visibility = View.INVISIBLE
            }

        }
    }

    private fun checkReadPermissionsAndOpenPdf(fileNamePath: String) {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE
        )
        if (checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            //open pdf
            val file = File(fileNamePath)
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().packageName + ".provider",
                file
            )
            intent.setDataAndType(uri, "application/pdf")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivity(intent)
        } else {
            showSnackBar(
                "please accept external read permissions", null,
                R.drawable.ic_baseline_error_outline_24, "", Color.RED
            )
        }
    }

    private fun checkWritePermissionAndProcessPdf() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE
        )
        if (checkSelfPermission(
                requireContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            processPdf()
        } else {
            showSnackBar(
                "please accept external write permissions",
                null,
                R.drawable.ic_baseline_error_outline_24, "",
                getColor(requireContext(), android.R.color.holo_red_dark)
            )
        }
    }

    private fun setupSwipeRefreshLayout() {
        binding.statsRefreshLayout.setOnRefreshListener {
            pdfDownloadsViewModel.getUserPdfDownloads()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBillingClient()
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(requireContext())
            .enablePendingPurchases()
            .setListener(this)
            .build()
        billingClient.startConnection(object : BillingClientStateListener {

            override fun onBillingServiceDisconnected() {
                showSnackBar(
                    "an error occurred, please retry again",
                    null,
                    R.drawable.ic_baseline_error_outline_24, "",
                    getColor(requireContext(), android.R.color.holo_red_dark)
                )
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is setup successfully
                    loadPdfSkus()
                }
            }

        })
    }


    private fun loadPdfSkus() = if (billingClient.isReady) {
        val params = SkuDetailsParams.newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.INAPP)
            .build()

        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (skuDetailsList != null) {
                    for (skuDetails in skuDetailsList) {
                        //this will return both the SKUs from Google Play Console
                        if (skuDetails.sku == skuList[0])
                            binding.pdfDownloadParent.setOnClickListener {
                                val billingFlowParams = BillingFlowParams
                                    .newBuilder()
                                    .setSkuDetails(skuDetails)
                                    .build()
                                billingClient.launchBillingFlow(
                                    requireActivity(),
                                    billingFlowParams
                                )
                            }
                    }
                }
            }

        }
    } else {
        //not necessary
        println("Billing client  not ready")
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            //purchase was successful
            for (purchase in purchases) {
                acknowledgePurchaseAndMarkAsConsumed(purchase.purchaseToken)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            showSnackBar(
                "purchase cancelled",
                null,
                R.drawable.ic_baseline_error_outline_24, "",
                getColor(requireContext(), android.R.color.holo_red_dark)
            )
        }
    }

    private fun acknowledgePurchaseAndMarkAsConsumed(purchaseToken: String) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()

        billingClient.consumeAsync(consumeParams) { billingResult, _ ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                //award 5 download coins
                userPdfDownloadsCount?.let {
                    it.noOfPdfDownloads += 5
                    pdfDownloadsViewModel.upsertUserPdfDownloads(it)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (checkSelfPermission(
                requireContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            processPdf()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }
}



