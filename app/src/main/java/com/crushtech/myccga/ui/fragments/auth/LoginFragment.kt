package com.crushtech.myccga.ui.fragments.auth

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.crushtech.myccga.MainActivity
import com.crushtech.myccga.R
import com.crushtech.myccga.data.remote.BasicAuthInterceptor
import com.crushtech.myccga.databinding.LoginLayoutBinding
import com.crushtech.myccga.ui.BaseFragment
import com.crushtech.myccga.utils.Constants.IS_THIRD_PARTY
import com.crushtech.myccga.utils.Constants.KEY_LOGGED_IN_EMAIL
import com.crushtech.myccga.utils.Constants.KEY_PASSWORD
import com.crushtech.myccga.utils.Constants.KEY_USERNAME
import com.crushtech.myccga.utils.Constants.NOT_THIRD_PARTY
import com.crushtech.myccga.utils.Constants.NO_EMAIL
import com.crushtech.myccga.utils.Constants.NO_PASSWORD
import com.crushtech.myccga.utils.Constants.NO_USERNAME
import com.crushtech.myccga.utils.Constants.RC_SIGN_IN
import com.crushtech.myccga.utils.Status
import com.crushtech.myccga.utils.viewLifecycle
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : BaseFragment(R.layout.login_layout) {
    private var binding: LoginLayoutBinding by viewLifecycle()

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
    private lateinit var credentials: AuthCredential

    private var currentEmail: String? = null
    private var currentPassword: String? = null
    private var currentUserName: String? = null
    private var isThirdParty = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LoginLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }


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

        binding.signUpRedirect.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }
        binding.LoginBtn.setOnClickListener {
            loginBtnWasClicked = true
            hideKeyboard()
            val email = binding.editTextEmail1.text.toString()
            val password = binding.editTextPassword1.text.toString()

            this.currentEmail = email
            this.currentPassword = password
            viewModel.login(email, password)
        }


        binding.FaceBookLoginBtn.setOnClickListener {
            //call on click on the main fb button
            binding.fbLoginButton.callOnClick()
        }
        binding.GoogleLoginBtn.setOnClickListener {
            //call on click on the main ga button
            binding.galoginButton.callOnClick()
            googleSignIn()
        }


        binding.fbLoginButton.apply {
            fragment = this@LoginFragment
            setReadPermissions("email", "public_profile")
            registerCallback(callbackManager, object : FacebookCallback<LoginResult> {

                override fun onSuccess(result: LoginResult?) {
                    handleFacebookToken(result?.accessToken)
                }

                override fun onCancel() {
                    showSnackBar(
                        "authentication cancelled",
                        null, R.drawable.ic_clear4,
                        "", Color.BLACK
                    )
                }

                override fun onError(error: FacebookException?) {
                    showSnackBar(
                        "an unknown error occurred",
                        null, R.drawable.ic_baseline_error_outline_24,
                        "", Color.RED
                    )
                    if (firebaseAuth.currentUser != null) {
                        LoginManager.getInstance().logOut()
                    }
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
            val credentials = FacebookAuthProvider.getCredential(it.token)
            firebaseAuth.signInWithCredential(credentials).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("signInWithCredential:success")
                    val user = firebaseAuth.currentUser
                    updateUI(user)
                } else if (!(task.isSuccessful) && task.exception is FirebaseAuthUserCollisionException) {
                    firebaseAuth.currentUser?.linkWithCredential(credentials)
                        ?.addOnCompleteListener { _task ->
                            if (_task.isSuccessful) {
                                val user = _task.result?.user
                                updateUI(user)
                            } else {
                                Timber.tag(TAG).w(_task.exception, "linking:failure")
                            }
                        }
                } else {
                    Timber.tag(TAG).w(task.exception, "signInWithCredential:failure")
                    showSnackBar(
                        "signInWithCredential:failure", null,
                        R.drawable.ic_baseline_error_outline_24,
                        "", Color.RED
                    )
                }
            }

        }
    }


    private fun subscribeToObservers() {
        viewModel.loginStatus.observe(viewLifecycleOwner, { result ->
            result?.let {
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let {
                            this.currentUserName = it
                        }
                        this.isThirdParty = false
                        resetUIForLoginButton()
                        val snackBarText = "Welcome back $currentUserName"
                        showSnackBar(
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
                        showSnackBar(
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

        viewModel.thirdPartyLoginStatus.observe(viewLifecycleOwner, { result ->
            result?.let {
                when (result.status) {
                    Status.SUCCESS -> {
                        this.isThirdParty = true
                        val snackBarText = "Welcome back $currentUserName"
                        showSnackBar(
                            snackBarText, null,
                            R.drawable.ic_baseline_whatshot_24,
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
                        showSnackBar(
                            snackBarText ?: "an error occurred", null,
                            R.drawable.ic_baseline_error_outline_24,
                            "", Color.RED
                        )
                    }
                    Status.LOADING -> {
                        showSnackBar(
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
            .requestIdToken(getString(R.string.itid_id))
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
    }

    private fun googleSignIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        credentials = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credentials).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Timber.d("signInWithCredential:success")
                val user = task.result?.user
                updateUI(user)
            } else {
                Timber.tag(TAG).w(task.exception, "signInWithCredential:failure")
                showSnackBar(
                    "signInWithCredential:failure", null,
                    R.drawable.ic_baseline_error_outline_24,
                    "", Color.RED
                )
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        user?.let { usr ->
            usr.email?.let { email ->
                if (currentEmail == NO_EMAIL || currentEmail.isNullOrEmpty()) {
                    currentEmail = email
                }
                usr.displayName?.let { username ->
                    this.currentUserName = username
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
            binding.LoginBtn.isAllCaps = false
            binding.LoginBtn.text = getString(R.string.loading_wait)
            binding.LoginProgressBar.visibility = View.VISIBLE
        }
    }

    private fun resetUIForLoginButton() {
        binding.LoginBtn.isAllCaps = true
        binding.LoginBtn.text = requireContext().getString(R.string.log_in)
        binding.LoginProgressBar.visibility = View.GONE
    }


    private fun createAnimationsForUIWidgets() {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_left)
        binding.lin1.startAnimation(animation)
        val animation1 = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_right)
        binding.LoginBtn.startAnimation(animation1)
        val animation2 = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_slowly)
        binding.logintxt.startAnimation(animation2)
        val animation3 = AnimationUtils.loadAnimation(requireContext(), R.anim.blink)
        binding.thirdPartyLoginLayoutParent.startAnimation(animation3)

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
                // Google Sign In failed
                Timber.w(e, "Google sign in failed")
                showSnackBar(
                    "Google sign in failed", null,
                    R.drawable.ic_baseline_error_outline_24,
                    "", Color.RED
                )
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}