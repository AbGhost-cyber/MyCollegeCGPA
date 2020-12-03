package com.crushtech.mycollegecgpa.utils

import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import com.crushtech.mycollegecgpa.R
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator


object Constants {
    val IGNORE_AUTH_URLS = listOf("/login", "register")
    const val SHIMMER_ITEM_NUMBER = 7
    const val DATABASE_NAME = "semester_db"

    const val BASE_URL = "https://mycollegecgpa.xyz/"

    const val GPA_MAX = 100

    const val ENCRYPTED_SHARED_PREF_NAME = "enc_shared_pref"

    const val KEY_LOGGED_IN_EMAIL = "KEY_LOGGED_IN_EMAIL"
    const val KEY_PASSWORD = "KEY_PASSWORD"
    const val KEY_USERNAME = "KEY_USERNAME"

    const val NO_EMAIL = "no email"
    const val NO_PASSWORD = "no password"
    const val NO_USERNAME = "no username"
    const val TOTAL_NUMBER_OF_CREDIT_HOURS = "totalNumberOfCreditHours"
    const val TOTAL_NUMBER_OF_COURSES = "totalNumberOfCourses"
    const val STATISTICS_FIRST_TIME_OPEN = "first time open"
    const val COURSE_FIRST_TIME_OPEN = "course first time open"
    const val IS_LOGGED_IN = "is logged in"

    const val PRIVACY_POLICY = "http://www.crushtech.unaux.com/privacypolicy/?i=1"

    fun setupDecorator(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int,
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
                        it.extend()
                    }
                }
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    list.forEach {
                        it.shrink()
                    }
                }
                if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    list.forEach {
                        it.extend()
                    }
                }
                super.onScrollStateChanged(recyclerView, newState)
            }

        }
    }
}


