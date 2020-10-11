package com.crushtech.mycollegecgpa.utils

import android.graphics.Canvas
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import com.crushtech.mycollegecgpa.R
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

object Constants {
    val IGNORE_AUTH_URLS = listOf("/login", "register")

    const val DATABASE_NAME = "semester_db"

    const val BASE_URL = "http://10.0.2.2:8080"

    const val ENCRYPTED_SHARED_PREF_NAME = "enc_shared_pref"

    const val KEY_LOGGED_IN_EMAIL = "KEY_LOGGED_IN_EMAIL"
    const val KEY_PASSWORD = "KEY_PASSWORD"
    const val KEY_USERNAME = "KEY_USERNAME"

    const val NO_EMAIL = "no email"
    const val NO_PASSWORD = "no password"
    const val NO_USERNAME = "no username"


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
}


