package com.crushtech.mycollegecgpa.ui.fragments.others

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.crushtech.mycollegecgpa.MainActivity
import com.crushtech.mycollegecgpa.R
import com.crushtech.mycollegecgpa.dialogs.LogoutDialogFragment
import com.crushtech.mycollegecgpa.ui.BaseFragment
import com.crushtech.mycollegecgpa.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.others_layout.*
import javax.inject.Inject

const val LOG_OUT_DIALOG = "log out dialog"

@AndroidEntryPoint
class OthersFragment : BaseFragment(R.layout.others_layout) {
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).hideAppBar()
        (activity as MainActivity).showMainActivityUI()

        logOut.setOnClickListener {
            showLogOutDialog()
        }
        if (savedInstanceState != null) {
            val logOutDialog = parentFragmentManager.findFragmentByTag(LOG_OUT_DIALOG)
                    as LogoutDialogFragment?
            logOutDialog?.setPositiveListener { clicked ->
                if (clicked) {
                    setupLogOutFunctionality()
                }
            }
        }
    }

    private fun showLogOutDialog() {
        LogoutDialogFragment().apply {
            setPositiveListener { clicked ->
                if (clicked) {
                    setupLogOutFunctionality()
                }
            }
        }.show(parentFragmentManager, LOG_OUT_DIALOG)
    }

    private fun setupLogOutFunctionality() {
        sharedPreferences.edit().putString(Constants.KEY_LOGGED_IN_EMAIL, Constants.NO_EMAIL)
            .apply()
        sharedPreferences.edit().putString(Constants.KEY_PASSWORD, Constants.NO_PASSWORD).apply()
        sharedPreferences.edit().putString(Constants.KEY_USERNAME, Constants.NO_USERNAME).apply()
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.homeFragment, true)
            .build()
        findNavController().navigate(
            OthersFragmentDirections.actionOthersFragmentToLoginFragment(),
            navOptions
        )
    }
}