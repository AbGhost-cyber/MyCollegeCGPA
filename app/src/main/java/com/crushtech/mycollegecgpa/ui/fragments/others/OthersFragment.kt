package com.crushtech.mycollegecgpa.ui.fragments.others

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.crushtech.mycollegecgpa.MainActivity
import com.crushtech.mycollegecgpa.R
import com.crushtech.mycollegecgpa.dialogs.LogoutDialogFragment
import com.crushtech.mycollegecgpa.ui.BaseFragment
import com.crushtech.mycollegecgpa.utils.Constants
import com.crushtech.mycollegecgpa.utils.Constants.PRIVACY_POLICY
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

        setClickAnimationForTexts(listOf(logOut, ShareApp, AboutApp, rateApp, privacyPolicy))

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

    private fun rateAppFunction() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "market://details?id="
                                + requireContext().packageName
                    )
                )
            )
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        getString(R.string.play_store_uri)
                                + requireContext().packageName
                    )
                )
            )
        }

    }


    private fun shareAppFunction() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        val appPackageName =
            requireContext().applicationContext.packageName
        val strAppLink: String
        strAppLink = try {
            getString(R.string.play_store_uri) + appPackageName
        } catch (anfe: ActivityNotFoundException) {
            getString(R.string.play_store_uri) + appPackageName
        }
        shareIntent.type = "text/link"
        val shareBody = getString(R.string.share_info) + strAppLink
        val shareSub = "APP NAME/TITLE"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub)
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(shareIntent, "Share Using"))
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
        sharedPreferences.edit().putString(
            Constants.KEY_LOGGED_IN_EMAIL,
            Constants.NO_EMAIL
        )
            .apply()
        sharedPreferences.edit().putString(
            Constants.KEY_PASSWORD,
            Constants.NO_PASSWORD
        ).apply()
        sharedPreferences.edit().putString(
            Constants.KEY_USERNAME,
            Constants.NO_USERNAME
        ).apply()
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.homeFragment, true)
            .build()
        findNavController().navigate(
            OthersFragmentDirections.actionOthersFragmentToLoginFragment(),
            navOptions
        )
    }

    private fun setClickAnimationForTexts(textLists: List<TextView>) {
        val animation = AnimationUtils.loadAnimation(
            requireContext(), android.R.anim.slide_in_left
        )
        textLists.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                when (index) {
                    0 -> showLogOutDialog()
                    1 -> shareAppFunction()
                    2 -> showSnackbar("amc")
                    3 -> rateAppFunction()
                    4 -> {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(PRIVACY_POLICY)
                        startActivity(intent)
                    }
                }
                textView.startAnimation(animation)
            }
        }
    }
}