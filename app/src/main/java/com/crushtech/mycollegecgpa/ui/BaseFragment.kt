package com.crushtech.mycollegecgpa.ui

import android.view.View
import androidx.fragment.app.Fragment
import com.crushtech.mycollegecgpa.utils.SimpleCustomSnackbar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

abstract class BaseFragment(layoutId: Int) : Fragment(layoutId) {
    fun showSnackbar(
        text: String, listener: View.OnClickListener?, iconId: Int,
        actionLabel: String, bgColor: Int
    ) {
        SimpleCustomSnackbar.make(
            requireActivity().parent_layout,
            text, Snackbar.LENGTH_LONG, listener,
            iconId, actionLabel,
            bgColor
        )?.show()
    }

}