package com.crushtech.mycollegecgpa.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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
        isCurrentlyActive: Boolean, textLabel: String,
        drawableId: Int, backgroundColor: Int = Color.RED, context: Context
    ) {
        val textTypeface = ResourcesCompat.getFont(
            context, R.font.averia_libre_bold
        )
        RecyclerViewSwipeDecorator.Builder(
            c,
            recyclerView,
            viewHolder,
            dX,
            dY,
            actionState,
            isCurrentlyActive
        ).addBackgroundColor(
            ContextCompat.getColor(
                context,
                backgroundColor
            )
        )
            .addActionIcon(drawableId)
            .addSwipeLeftLabel(textLabel)
            .setSwipeLeftLabelTypeface(textTypeface)
            .setSwipeLeftLabelColor(Color.WHITE)
            .setSwipeLeftLabelTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
            .create()
            .decorate()
    }
}


