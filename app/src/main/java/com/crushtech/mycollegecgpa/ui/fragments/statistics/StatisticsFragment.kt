package com.crushtech.mycollegecgpa.ui.fragments.statistics

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
import androidx.lifecycle.Observer
import com.android.billingclient.api.BillingClient
import com.crushtech.mycollegecgpa.MainActivity
import com.crushtech.mycollegecgpa.R
import com.crushtech.mycollegecgpa.data.local.entities.UserPdfDownloads
import com.crushtech.mycollegecgpa.ui.BaseFragment
import com.crushtech.mycollegecgpa.ui.fragments.home.HomeViewModel
import com.crushtech.mycollegecgpa.utils.Constants
import com.crushtech.mycollegecgpa.utils.Constants.STATISTICS_FIRST_TIME_OPEN
import com.crushtech.mycollegecgpa.utils.Constants.TOTAL_NUMBER_OF_COURSES
import com.crushtech.mycollegecgpa.utils.Constants.TOTAL_NUMBER_OF_CREDIT_HOURS
import com.crushtech.mycollegecgpa.utils.CustomMarkerView
import com.crushtech.mycollegecgpa.utils.NetworkUtils
import com.crushtech.mycollegecgpa.utils.Status
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.statistics_fragment.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


const val REQUEST_CODE = 1

@AndroidEntryPoint
class StatisticsFragment : BaseFragment(R.layout.statistics_fragment) {
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (!(sharedPrefs.contains(STATISTICS_FIRST_TIME_OPEN))) {
            firsTimeOpen = true
            sharedPrefs.edit().putBoolean(STATISTICS_FIRST_TIME_OPEN, true).apply()
        }
        return inflater.inflate(R.layout.statistics_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pdfDownloadsViewModel.getUserPdfDownloads()
        (activity as MainActivity).apply {
            showAppBar()
            hideMainActivityUI()
            mainLayoutToolbar.setNavigationIcon(R.drawable.ic_baseline_chevron_left_24)
        }
        requireActivity().titleBarText.text = "My Statistics"
        setUpObservers()
        setUpBarCharts()
        setupSwipeRefreshLayout()

        val totalCH = sharedPrefs
            .getFloat(TOTAL_NUMBER_OF_CREDIT_HOURS, 0F)
        val totalCC = sharedPrefs
            .getInt(TOTAL_NUMBER_OF_COURSES, 0)

        totalCourseChange.observe(viewLifecycleOwner, Observer {
            when {
                it == totalCC -> {
                    courseChange.text = "0"
                }
                it > totalCC -> {

                    val result = it - totalCC
                    courseChange.text = result.toString()
                }
                else -> {
                    val result = it - totalCC
                    courseChange.text = result.toString()
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


            if (courseChange.text.contains("-")) {
                courseChange.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_round_arrow_drop_down_24, 0, 0, 0
                )
                courseChange.setTextColor(getColor(requireContext(), android.R.color.holo_red_dark))
            } else {
                courseChange.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_baseline_arrow_drop_up_24, 0, 0, 0
                )
                courseChange.setTextColor(getColor(requireContext(), R.color.progress_color))
            }
        })
        totalCreditHoursChange.observe(viewLifecycleOwner, Observer {
            when {
                it == totalCH -> {
                    creditHoursChange.text = "0"
                }
                it > totalCH -> {
                    val result = it - totalCH
                    creditHoursChange.text = result.toString()
                }
                totalCH > it -> {
                    val result = it - totalCH
                    creditHoursChange.text = result.toString()
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



            if (creditHoursChange.text.contains("-")) {
                creditHoursChange.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_round_arrow_drop_down_24, 0, 0, 0
                )
                creditHoursChange.setTextColor(
                    getColor(
                        requireContext(),
                        android.R.color.holo_red_dark
                    )
                )
            } else {
                creditHoursChange.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_baseline_arrow_drop_up_24, 0, 0, 0
                )
                creditHoursChange.setTextColor(getColor(requireContext(), R.color.progress_color))
            }
        })
        NetworkUtils.getNetworkLiveData(requireContext()).observe(viewLifecycleOwner,
            Observer { content ->
                val isConnected = content.peekContent()
                if (isConnected) {
                    pdfDownloadsViewModel.getUserPdfDownloads()
                }
                saveAsPdf.setOnClickListener {
                    if (isConnected) {
                        if (viewPdf.isVisible) {
                            viewPdf.visibility = View.GONE
                        }
                        userPdfDownloadsCount?.let { downloads ->
                            if (downloads.noOfPdfDownloads <= 0) {
                                showSnackbar(
                                    "you've ran out of download points, please purchase", null,
                                    R.drawable.ic_baseline_error_outline_24,
                                    "", Color.RED
                                )
                            } else {
                                checkWritePermissionAndProcessPdf()
                            }
                        }
                    } else {
                        showSnackbar(
                            "internet connection is required for this feature",
                            null,
                            R.drawable.ic_baseline_error_outline_24,
                            "", Color.RED
                        )
                    }

                }
                pdfDownloadParent.setOnClickListener {
                    if (isConnected) {
                        userPdfDownloadsCount?.let {
                            it.noOfPdfDownloads += 5
                            pdfDownloadsViewModel.upsertUserPdfDownloads(it)
                        }
                    } else {
                        showSnackbar(
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
        viewModel.allSemesters.observe(viewLifecycleOwner, Observer {
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
                        totalCoursesOffered.text = courseString
                        sem.courses.forEach { course ->
                            totalNumberOfCreditHours += course.creditHours
                            val creditHoursString = if (totalNumberOfCreditHours <= 1) {
                                "$totalNumberOfCreditHours hour"
                            } else {
                                "$totalNumberOfCreditHours hours"
                            }
                            tvCHours.text = creditHoursString
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
                    val typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.capriola)


                    val colorList = listOf(
                        getColor(requireContext(), R.color.colorPrimary),
                        getColor(requireContext(), R.color.colorAccent)
                    )

                    val barDataSet = BarDataSet(allGPA, "GPA per semester").apply {
                        valueTextColor = Color.BLACK
                        valueTypeface = typeface
                        valueTextSize = 13f
                        colors = colorList

                    }

                    barChart.data = BarData(barDataSet)
                    barChart.invalidate()
                    barChart.marker =
                        CustomMarkerView(semesters, requireContext(), R.layout.marker_view)
                    barChart.description.isEnabled = false
                }
            }

        })

        pdfDownloadsViewModel.pdfDownloads.observe(viewLifecycleOwner, Observer { results ->
            when (results.status) {
                Status.SUCCESS -> {
                    val downloads = results.data!!
                    pdfDownloadCounts.text = downloads.noOfPdfDownloads.toString()
                    userPdfDownloadsCount = downloads
                    statsRefreshLayout.isRefreshing = false
                    pdfDownloadsViewModel.getUserPdfDownloads()

                }
                Status.ERROR -> {
                    showSnackbar(
                        results.message ?: "an error occurred", null,
                        R.drawable.ic_baseline_error_outline_24,
                        "", Color.RED
                    )
                }
                Status.LOADING -> {
                    statsRefreshLayout.isRefreshing = false
                }
            }
        })


    }


    private fun setUpBarCharts() {
        barChart.xAxis.apply {
            position = XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.BLACK
            textColor = Color.BLACK
            setDrawGridLines(false)
        }
        barChart.axisLeft.apply {
            axisLineColor = Color.BLACK
            textColor = Color.BLACK
            setDrawGridLines(false)

        }
        barChart.axisRight.apply {
            axisLineColor = Color.BLACK
            textColor = Color.BLACK
            setDrawGridLines(false)

        }
    }


    private fun processPdf() {
        val pageInfo = PdfDocument.PageInfo.Builder(2250, 1400, 1).create()
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
        saveAsPdf.visibility = View.INVISIBLE
        pdfDownloadParent.visibility = View.GONE
        sponsored.visibility = View.VISIBLE
        val username = " For ${Constants.getCurrentUserName(sharedPrefs)}"
        user_name.text = username
        user_name.visibility = View.VISIBLE

        //if barchart markerview was visible, then hide it while creating the pdf
        try {
            barChart.highlightValue(null)
        } catch (e: Exception) {
        }

        content.draw(page.canvas)

        document.finishPage(page)
        val pdfFolder = File(
            requireContext().externalCacheDir?.absolutePath,
            " COLLEGE CGPA"
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
            (activity as MainActivity).progressBg.visibility = View.VISIBLE
            pdf_pb.apply {
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
            pdf_pb.visibility = View.GONE
            (activity as MainActivity).progressBg.visibility = View.GONE
            if (pdfHasBeenCreated) {
                userPdfDownloadsCount?.let {
                    it.noOfPdfDownloads--
                    pdfDownloadsViewModel.upsertUserPdfDownloads(it)
                    pdfHasBeenCreated = false
                }
                showSnackbar(
                    "PDF CREATED SUCCESSFULLY", null,
                    R.drawable.ic_baseline_bubble_chart_24, "", Color.BLACK
                )
                viewPdf.visibility = View.VISIBLE

                viewPdf.setOnClickListener {
                    checkReadPermissionsAndOpenPdf(fileNamePath)
                }
                val countDown = 4000L
                val countDownTimer = object : CountDownTimer(countDown, 1000) {
                    override fun onFinish() {
                        try {
                            viewPdf.visibility = View.GONE
                        } catch (e: Exception) {
                        }
                    }

                    override fun onTick(p0: Long) {
                        val text = "View Pdf ${p0 / 1000}"
                        try {
                            viewPdf.text = text
                        } catch (e: Exception) {
                        }
                    }

                }
                if (viewPdf.visibility == View.VISIBLE) {
                    countDownTimer.start()
                }

                saveAsPdf.visibility = View.VISIBLE
                pdfDownloadParent.visibility = View.VISIBLE
                content.updateLayoutParams {
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
                sponsored.visibility = View.INVISIBLE
                user_name.visibility = View.INVISIBLE
            } else {
                showSnackbar(
                    "an unknown error occurred, please try again", null,
                    R.drawable.ic_baseline_error_outline_24, "", Color.RED
                )
                saveAsPdf.visibility = View.VISIBLE
                content.updateLayoutParams {
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
                sponsored.visibility = View.INVISIBLE
                user_name.visibility = View.INVISIBLE
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
            showSnackbar(
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
            showSnackbar(
                "please accept external write permissions",
                null,
                R.drawable.ic_baseline_error_outline_24, "",
                getColor(requireContext(), android.R.color.holo_red_dark)
            )
        }
    }

    private fun setupSwipeRefreshLayout() {
        statsRefreshLayout.setOnRefreshListener {
            pdfDownloadsViewModel.getUserPdfDownloads()
        }
    }
}