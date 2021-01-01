package com.crushtech.mycollegecgpa.ui.fragments.auth

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.crushtech.mycollegecgpa.MainActivity
import com.crushtech.mycollegecgpa.R
import com.crushtech.mycollegecgpa.data.remote.BasicAuthInterceptor
import com.crushtech.mycollegecgpa.ui.BaseFragment
import com.crushtech.mycollegecgpa.utils.Constants.IS_THIRD_PARTY
import com.crushtech.mycollegecgpa.utils.Constants.KEY_LOGGED_IN_EMAIL
import com.crushtech.mycollegecgpa.utils.Constants.KEY_PASSWORD
import com.crushtech.mycollegecgpa.utils.Constants.KEY_USERNAME
import com.crushtech.mycollegecgpa.utils.Constants.NOT_THIRD_PARTY
import com.crushtech.mycollegecgpa.utils.Constants.NO_EMAIL
import com.crushtech.mycollegecgpa.utils.Constants.NO_PASSWORD
import com.crushtech.mycollegecgpa.utils.Constants.NO_USERNAME
import com.crushtech.mycollegecgpa.utils.Constants.RC_SIGN_IN
import com.crushtech.mycollegecgpa.utils.Status
import com.facebook.*
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.login_layout.*
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : BaseFragment(R.layout.login_layout) {
    @Inject
    lateinit var sharedPrefs: SharedPreferences

    private val TAG = "google auth"

    @Inject
    lateinit var basicAuthInterceptor: BasicAuthInterceptor

    private val viewModel: AuthViewModel by viewModels()
    private var loginBtnWasClicked = false

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private lateinit var callbackManager: CallbackManager
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var accessTokenTracker: AccessTokenTracker
    private var credentials: AuthCredential? = null

    private var currentEmail: String? = null
    private var currentPassword: String? = null
    private var currentUserName: String? = null
    private var isThirdParty = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).apply {
            hideMainActivityUI()
            hideAppBar()
            window.setFlags(
                FLAG_FULLSCREEN,
                FLAG_FULLSCREEN
            )
        }

        firebaseAuth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

        createAnimationsForUIWidgets()
        if (isLoggedIn()) {
            authenticateApi(
                currentEmail ?: "",
                currentPassword ?: ""
            )
            redirectLogin()
        }

        //set screen orientation to portrait
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        subscribeToObservers()

        signUpRedirect.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }
        LoginBtn.setOnClickListener {
            loginBtnWasClicked = true
            hideKeyboard()
            val email = editTextEmail1.text.toString()
            val password = editTextPassword1.text.toString()

            this.currentEmail = email
            this.currentPassword = password
            viewModel.login(email, password)
        }


        FaceBookLoginBtn.setOnClickListener {
            //call on click on the main fb button
            fb_login_button.callOnClick()
        }
        GoogleLoginBtn.setOnClickListener {
            //call on clcik on the main ga button
            ga_login_button.callOnClick()
            googleSignIn()
        }


        fb_login_button.apply {
            fragment = this@LoginFragment
            setReadPermissions("email", "public_profile")
            registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    handleFacebookToken(result?.accessToken)
                }

                override fun onCancel() {
                    showSnackbar(
                        "authentication cancelled",
                        null, R.drawable.ic_clear4,
                        "", Color.BLACK
                    )
                }

                override fun onError(error: FacebookException?) {
                    showSnackbar(
                        "an unknown error occurred",
                        null, R.drawable.ic_baseline_error_outline_24,
                        "", Color.RED
                    )
                }

            })
        }


        accessTokenTracker = object : AccessTokenTracker() {
            override fun onCurrentAccessTokenChanged(
                oldAccessToken: AccessToken?,
                currentAccessToken: AccessToken?
            ) {
                if (currentAccessToken == null) {
                    firebaseAuth.signOut()
                }
            }

        }
    }


    private fun handleFacebookToken(token: AccessToken?) {
        token?.let {
            credentials = FacebookAuthProvider.getCredential(it.token)
            credentials?.let { authCredential ->
                firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener { task ->
                    if (task.isComplete) {
                        val user = firebaseAuth.currentUser
                        updateUI(user)
                    } else {
                        showSnackbar(
                            "Authentication error", null,
                            R.drawable.ic_baseline_error_outline_24,
                            "", Color.RED
                        )
                    }
                }
            }
        }
    }


    private fun subscribeToObservers() {
        viewModel.loginStatus.observe(viewLifecycleOwner, Observer { result ->
            result?.let {
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let {
                            this.currentUserName = it
                        }
                        this.isThirdParty = false
                        resetUIForLoginButton()
                        val snackBarText = "Welcome back $currentUserName"
                        showSnackbar(
                            snackBarText, null, R.drawable.ic_baseline_whatshot_24,
                            "", Color.BLACK
                        )
                        sharedPrefs.edit().putBoolean(
                            IS_THIRD_PARTY,
                            false
                        ).apply()

                        sharedPrefs.edit().putString(
                            KEY_LOGGED_IN_EMAIL,
                            currentEmail
                        ).apply()
                        sharedPrefs.edit().putString(
                            KEY_USERNAME,
                            currentUserName
                        ).apply()

                        authenticateApi(
                            currentEmail ?: "",
                            currentPassword ?: ""
                        )
                        redirectLogin()
                    }
                    Status.ERROR -> {
                        resetUIForLoginButton()
                        val snackBarText = result.message
                        showSnackbar(
                            snackBarText ?: "an error occurred", null,
                            R.drawable.ic_baseline_error_outline_24,
                            "", Color.RED
                        )
                    }
                    Status.LOADING -> {
                        updateUIForLoginButton()
                    }
                }
            }
        })

        viewModel.thirdPartyLoginStatus.observe(viewLifecycleOwner, Observer { result ->
            result?.let {
                when (result.status) {
                    Status.SUCCESS -> {
                        this.isThirdParty = true
                        val snackBarText = "Welcome back $currentUserName"
                        showSnackbar(
                            snackBarText, null, R.drawable.ic_baseline_whatshot_24,
                            "", Color.BLACK
                        )
                        sharedPrefs.edit().putBoolean(
                            IS_THIRD_PARTY,
                            true
                        ).apply()
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

                        authenticateApi(currentEmail ?: "")
                        redirectLogin()
                    }
                    Status.ERROR -> {
                        val snackBarText = result.message
                        showSnackbar(
                            snackBarText ?: "an error occurred", null,
                            R.drawable.ic_baseline_error_outline_24,
                            "", Color.RED
                        )
                    }
                    Status.LOADING -> {
                        showSnackbar(
                            "please hold on for a moment.....",
                            null,
                            R.drawable.ic_baseline_error_outline_24,
                            "", Color.BLACK,
                            Snackbar.LENGTH_INDEFINITE
                        )
                    }
                }
            }
        })
    }


    private fun createGoogleSignInRequest() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
    }

    private fun googleSignIn() {
        showSnackbar(
            "Google sign in starts", null,
            R.drawable.ic_baseline_error_outline_24,
            "", Color.RED
        )
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)

    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        credentials = GoogleAuthProvider.getCredential(idToken, null)
        credentials?.let { authCredential ->
            firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("signInWithCredential:success")
                    val user = firebaseAuth.currentUser
                    updateUI(user)
                } else {
                    Timber.tag(TAG).w(task.exception, "signInWithCredential:failure")
                    showSnackbar(
                        "signInWithCredential:failure", null,
                        R.drawable.ic_baseline_error_outline_24,
                        "", Color.RED
                    )
                }
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        user?.let { usr ->
            usr.email?.let { email ->
                if (currentEmail == NO_EMAIL || currentEmail.isNullOrEmpty()) {
                    currentEmail = email
                    this.currentUserName = usr.displayName
                }
                usr.displayName?.let { username ->
                    viewModel.loginThirdPartyUser(email, username)
                }
            }
        }
    }


    private fun authenticateApi(email: String, password: String = "") {
        if (isThirdParty) {
            basicAuthInterceptor.email = email
        } else {
            basicAuthInterceptor.email = email
            basicAuthInterceptor.password = password
        }
    }

    private fun redirectLogin() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.chooseLoginOrSignUpFragment, true)
            .build()
        findNavController().navigate(
            LoginFragmentDirections.actionLoginFragmentToHomeFragment(),
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
        isThirdParty = sharedPrefs.getBoolean(
            IS_THIRD_PARTY,
            NOT_THIRD_PARTY
        )
        return if (isThirdParty) {
            //auth with email
            currentEmail != NO_EMAIL
        } else {
            //basic auth requires email & password
            currentEmail != NO_EMAIL && currentPassword != NO_PASSWORD
        }
    }

    private fun updateUIForLoginButton() {
        if (loginBtnWasClicked) {
            LoginBtn.isAllCaps = false
            LoginBtn.text = getString(R.string.loading_wait)
            LoginProgressBar.visibility = View.VISIBLE
        }
    }

    private fun resetUIForLoginButton() {
        LoginBtn.isAllCaps = true
        LoginBtn.text = requireContext().getString(R.string.log_in)
        LoginProgressBar.visibility = View.GONE
    }


    private fun createAnimationsForUIWidgets() {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_left)
        lin1.startAnimation(animation)
        val animation1 = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_right)
        LoginBtn.startAnimation(animation1)
        val animation2 = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_slowly)
        logintxt.startAnimation(animation2)
        val animation3 = AnimationUtils.loadAnimation(requireContext(), R.anim.blink)
        thirdPartyLoginLayoutParent.startAnimation(animation3)

    }

    override fun onDetach() {
        super.onDetach()
        (activity as MainActivity)
            .window.clearFlags(
                FLAG_FULLSCREEN
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createGoogleSignInRequest()
        val onBackPressed = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_loginFragment_to_chooseLoginOrSignUpFragment)
            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressed)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //facebook call back
        callbackManager.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent()
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val googleAccount = task.getResult(ApiException::class.java)!!
                Timber.d("firebaseAuthWithGoogle:%s", googleAccount.id)
                firebaseAuthWithGoogle(googleAccount.idToken!!)

            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Timber.w(e, "Google sign in failed")
                showSnackbar(
                    "Google sign in failed", null,
                    R.drawable.ic_baseline_error_outline_24,
                    "", Color.RED
                )
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}