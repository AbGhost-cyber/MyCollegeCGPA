package com.crushtech.mycollegecgpa.ui.fragments.others

import android.os.Bundle
import android.view.View
import com.crushtech.mycollegecgpa.MainActivity
import com.crushtech.mycollegecgpa.R
import com.crushtech.mycollegecgpa.ui.BaseFragment

class OthersFragment : BaseFragment(R.layout.others_layout) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).hideAppBar()
        (activity as MainActivity).showMainActivityUI()
    }
}