package com.crushtech.mycollegecgpa.ui

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
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

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}