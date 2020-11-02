package com.crushtech.mycollegecgpa.ui.fragments

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import android.widget.TextView
import android.widget.TextView.BufferType.SPANNABLE
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.crushtech.mycollegecgpa.MainActivity
import com.crushtech.mycollegecgpa.R
import com.crushtech.mycollegecgpa.R.id.action_chooseLoginOrSignUpFragment_to_loginFragment
import com.crushtech.mycollegecgpa.R.id.action_chooseLoginOrSignUpFragment_to_signUpFragment
import com.crushtech.mycollegecgpa.data.remote.BasicAuthInterceptor
import com.crushtech.mycollegecgpa.ui.BaseFragment
import com.crushtech.mycollegecgpa.ui.fragments.ChooseLoginOrSignUpFragmentDirections.Companion.actionChooseLoginOrSignUpFragmentToHomeFragment
import com.crushtech.mycollegecgpa.utils.Constants.IS_LOGGED_IN
import com.crushtech.mycollegecgpa.utils.Constants.KEY_LOGGED_IN_EMAIL
import com.crushtech.mycollegecgpa.utils.Constants.KEY_PASSWORD
import com.crushtech.mycollegecgpa.utils.Constants.NO_EMAIL
import com.crushtech.mycollegecgpa.utils.Constants.NO_PASSWORD
import com.crushtech.mycollegecgpa.utils.Constants.PRIVACY_POLICY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.choose_login_signup_layout.*
import javax.inject.Inject

@AndroidEntryPoint
class ChooseLoginOrSignUpFragment : BaseFragment(R.layout.choose_login_signup_layout) {
    @Inject
    lateinit var sharedPrefs: SharedPreferences

    @Inject
    lateinit var basicAuthInterceptor: BasicAuthInterceptor

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).apply {
            hideMainActivityUI()
            hideAppBar()
            window.setFlags(
                FLAG_FULLSCREEN,
                FLAG_FULLSCREEN
            )
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        val userIsLoggedIn = sharedPrefs.getBoolean(IS_LOGGED_IN, false)

        if (userIsLoggedIn) {
            val currentEmail = sharedPrefs.getString(
                KEY_LOGGED_IN_EMAIL,
                NO_EMAIL
            ) ?: NO_EMAIL
            val currentPassword = sharedPrefs.getString(
                KEY_PASSWORD,
                NO_PASSWORD
            ) ?: NO_PASSWORD
            basicAuthInterceptor.email = currentEmail
            basicAuthInterceptor.password = currentPassword
            redirectLogin()
        }
        privacyPolicy.makeLinks(Pair("Terms & Privacy Policy", View.OnClickListener {
            val intent = Intent(ACTION_VIEW)
            intent.data = Uri.parse(PRIVACY_POLICY)
            startActivity(intent)
        }))
        signUpScreen.setOnClickListener {
            findNavController().navigate(
                action_chooseLoginOrSignUpFragment_to_signUpFragment
            )
        }
        loginScreen.setOnClickListener {
            findNavController().navigate(
                action_chooseLoginOrSignUpFragment_to_loginFragment
            )
        }

    }


    //for onboarding term of service and privacy policy
    private fun TextView.makeLinks(vararg links: Pair<String, View.OnClickListener>) {
        val spannableString = SpannableString(this.text)
        for (link in links) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(p0: View) {
                    Selection.setSelection((p0 as TextView).text as Spannable, 0)
                    p0.invalidate()
                    link.second.onClick(p0)
                }

            }
            val startIndexOfLink = this.text.toString().indexOf(link.first)
            spannableString.setSpan(
                clickableSpan, startIndexOfLink,
                startIndexOfLink + link.first.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        this.movementMethod = LinkMovementMethod.getInstance()
        this.setText(spannableString, SPANNABLE)

    }

    private fun redirectLogin() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.chooseLoginOrSignUpFragment, true)
            .build()
        findNavController().navigate(
            actionChooseLoginOrSignUpFragmentToHomeFragment(),
            navOptions
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().window.setFlags(
            FLAG_FULLSCREEN,
            FLAG_FULLSCREEN
        )
    }
}