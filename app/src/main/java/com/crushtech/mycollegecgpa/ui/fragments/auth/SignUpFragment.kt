package com.crushtech.mycollegecgpa.ui.fragments.auth

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.crushtech.mycollegecgpa.MainActivity
import com.crushtech.mycollegecgpa.R
import com.crushtech.mycollegecgpa.data.remote.BasicAuthInterceptor
import com.crushtech.mycollegecgpa.ui.BaseFragment
import com.crushtech.mycollegecgpa.utils.Constants.KEY_LOGGED_IN_EMAIL
import com.crushtech.mycollegecgpa.utils.Constants.KEY_PASSWORD
import com.crushtech.mycollegecgpa.utils.Constants.KEY_USERNAME
import com.crushtech.mycollegecgpa.utils.Constants.NO_EMAIL
import com.crushtech.mycollegecgpa.utils.Constants.NO_PASSWORD
import com.crushtech.mycollegecgpa.utils.Constants.NO_USERNAME
import com.crushtech.mycollegecgpa.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.signup_layout.*
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SignUpFragment : BaseFragment(R.layout.signup_layout) {
    @Inject
    lateinit var sharedPrefs: SharedPreferences

    @Inject
    lateinit var basicAuthInterceptor: BasicAuthInterceptor

    private val viewModel: AuthViewModel by viewModels()

    private var currentEmail: String? = null
    private var currentPassword: String? = null
    private var currentUserName: String? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).apply {
            hideMainActivityUI()
            hideAppBar()
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        createAnimationsForUIWidgets()
        //set screen orientation to portrait
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        if (isLoggedIn()) {
            authenticateApi(
                currentEmail ?: "",
                currentPassword ?: ""
            )
            redirectLogin()
        }
        subscribeToObservers()
        loginRedirect.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
        }



        signUpBtn.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            val reEnterPassword = editTextReEnterPassword.text.toString()
            val username = editTextUsername.text.toString()

            this.currentEmail = email
            this.currentPassword = password
            this.currentUserName = username

            viewModel.register(email, password, reEnterPassword, username)
        }
    }

    private fun subscribeToObservers() {
        val progressBg: LinearLayout = (activity as MainActivity).progressBg
        viewModel.registerStatus.observe(viewLifecycleOwner, Observer { result ->
            result?.let {
                when (result.status) {
                    Status.SUCCESS -> {
                        progressBg.visibility = View.GONE
                        progressImage.visibility = View.GONE
                        signUpprogressBar.visibility = View.GONE
                        showSnackbar(
                            "Successfully registered"
                        )
                        sharedPrefs.edit().putString(
                            KEY_LOGGED_IN_EMAIL,
                            currentEmail
                        ).apply()
                        sharedPrefs.edit().putString(
                            KEY_PASSWORD,
                            currentPassword
                        ).apply()
                        sharedPrefs.edit().putString(
                            KEY_USERNAME,
                            currentUserName
                        ).apply()

                        authenticateApi(
                            currentEmail ?: "",
                            currentPassword ?: ""
                        )
                        Timber.d("CALLED")
                        redirectLogin()
                    }
                    Status.ERROR -> {
                        progressBg.visibility = View.GONE
                        progressImage.visibility = View.GONE
                        signUpprogressBar.visibility = View.GONE
                        showSnackbar(
                            result.message ?: "An unknown error occurred"
                        )
                        editTextPassword.text.clear()
                        editTextReEnterPassword.text?.clear()
                    }
                    Status.LOADING -> {
                        progressBg.visibility = View.VISIBLE
                        progressImage.visibility = View.VISIBLE
                        signUpprogressBar.visibility = View.VISIBLE
                    }
                }
            }
        })
    }


    private fun authenticateApi(email: String, password: String) {
        basicAuthInterceptor.email = email
        basicAuthInterceptor.password = password
    }

    private fun redirectLogin() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.signUpFragment, true)
            .build()
        findNavController().navigate(
            SignUpFragmentDirections.actionSignUpFragmentToHomeFragment(),
            navOptions
        )
    }

    private fun isLoggedIn(): Boolean {
        currentEmail = sharedPrefs.getString(
            KEY_LOGGED_IN_EMAIL,
            NO_EMAIL
        ) ?: NO_EMAIL
        currentPassword = sharedPrefs.getString(
            KEY_PASSWORD,
            NO_PASSWORD
        ) ?: NO_PASSWORD
        currentUserName = sharedPrefs.getString(
            KEY_USERNAME,
            NO_USERNAME
        ) ?: NO_USERNAME

        return currentEmail != NO_EMAIL && currentPassword !=
                NO_PASSWORD && currentUserName != NO_USERNAME
    }

    private fun createAnimationsForUIWidgets() {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_left)
        lin.startAnimation(animation)
        val animation1 = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_right)
        customImage1.startAnimation(animation1)
        customImage2.startAnimation(animation1)
        signUpBtn.startAnimation(animation1)
    }

    override fun onDetach() {
        super.onDetach()
        (activity as MainActivity)
            .window.clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
    }
}