package com.crushtech.myccgpa.utils

import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.crushtech.myccgpa.R
import com.crushtech.myccgpa.data.local.entities.GradeClass
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

object Constants {
    val IGNORE_AUTH_URLS = listOf(
        "/login", "/register", "/fbLogin",
        "/fbRegister", "/googleLogin", "/googleRegister"
    )
    const val DATABASE_NAME = "semester_db"

    const val BASE_URL = "https://mycollegecgpa.xyz/"

    const val WEIGHT_MAX = 20

    const val ENCRYPTED_SHARED_PREF_NAME = "enc_shared_pref"

    const val KEY_LOGGED_IN_EMAIL = "KEY_LOGGED_IN_EMAIL"
    const val KEY_PASSWORD = "KEY_PASSWORD"
    const val KEY_USERNAME = "KEY_USERNAME"
    const val IS_THIRD_PARTY = "is third party"
    const val NO_EMAIL = "no email"
    const val NO_PASSWORD = "no password"
    const val NO_USERNAME = "no username"
    const val NOT_THIRD_PARTY = false
    const val RC_SIGN_IN = 1

    const val TOTAL_NUMBER_OF_CREDIT_HOURS = "totalNumberOfCreditHours"
    const val TOTAL_NUMBER_OF_COURSES = "totalNumberOfCourses"
    const val STATISTICS_FIRST_TIME_OPEN = "first time open"
    const val IS_LOGGED_IN = "is logged in"

    const val PRIVACY_POLICY = "https://www.mycollegecgpa.com/privacypolicy/"
    const val ONESIGNAL_APP_ID = "ab830271-41ae-4e33-a673-23414a8c9ba2"
    const val ACTION_SHOW_SEM_REQ_FRAGMENT = "show semester request fragment"

    fun setupDecorator(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        RecyclerViewSwipeDecorator.Builder(
            c,
            recyclerView,
            viewHolder,
            dX,
            dY,
            actionState,
            isCurrentlyActive
        ).addSwipeRightActionIcon(R.drawable.ic_email)
            .addSwipeLeftActionIcon(R.drawable.ic_baseline_delete_24)
            .setSwipeLeftLabelColor(Color.WHITE)
            .setSwipeRightLabelColor(Color.WHITE)
            .addSwipeLeftBackgroundColor(Color.RED)
            .addSwipeRightBackgroundColor(Color.GREEN)
            .create()
            .decorate()
    }

    fun getCurrentUserName(sharedPrefs: SharedPreferences): String {
        return sharedPrefs.getString(
            KEY_USERNAME,
            NO_USERNAME
        ) ?: NO_USERNAME
    }

    fun customRecyclerViewScrollListener(list: List<ExtendedFloatingActionButton>): RecyclerView.OnScrollListener {
        return object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    list.forEach {
                        it.visibility = View.VISIBLE
                    }
                }
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    list.forEach {
                        it.visibility = View.GONE
                    }
                }
                if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    list.forEach {
                        it.visibility = View.VISIBLE
                    }
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        }
    }

    fun getHighestGrade(gradePoints: GradeClass): Float? {
        val transformedList = ArrayList<Float>()
        transformedList.add(gradePoints.APlusGrade)
        transformedList.add(gradePoints.AMinusGrade)
        transformedList.add(gradePoints.BPlusGrade)
        transformedList.add(gradePoints.BGrade)
        transformedList.add(gradePoints.BMinusGrade)
        transformedList.add(gradePoints.CPlusGrade)
        transformedList.add(gradePoints.CGrade)
        transformedList.add(gradePoints.CMinusGrade)
        transformedList.add(gradePoints.DPlusGrade)
        transformedList.add(gradePoints.DGrade)
        transformedList.add(gradePoints.FOrEGrade)
        return transformedList.maxOrNull()
    }

    inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
        crossinline bindingInflater: (LayoutInflater) -> T
    ) =
        lazy(LazyThreadSafetyMode.NONE) {
            bindingInflater.invoke(layoutInflater)
        }
}
