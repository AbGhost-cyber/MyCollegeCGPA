package com.crushtech.mycollegecgpa.ui.fragments.auth

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.crushtech.mycollegecgpa.MainActivity
import com.crushtech.mycollegecgpa.R
import com.crushtech.mycollegecgpa.data.remote.BasicAuthInterceptor
import com.crushtech.mycollegecgpa.databinding.SignupLayoutBinding
import com.crushtech.mycollegecgpa.ui.BaseFragment
import com.crushtech.mycollegecgpa.utils.Constants
import com.crushtech.mycollegecgpa.utils.Constants.IS_THIRD_PARTY
import com.crushtech.mycollegecgpa.utils.Constants.KEY_LOGGED_IN_EMAIL
import com.crushtech.mycollegecgpa.utils.Constants.KEY_PASSWORD
import com.crushtech.mycollegecgpa.utils.Constants.KEY_USERNAME
import com.crushtech.mycollegecgpa.utils.Constants.NOT_THIRD_PARTY
import com.crushtech.mycollegecgpa.utils.Constants.NO_EMAIL
import com.crushtech.mycollegecgpa.utils.Constants.NO_PASSWORD
import com.crushtech.mycollegecgpa.utils.Constants.NO_USERNAME
import com.crushtech.mycollegecgpa.utils.Status
import com.crushtech.mycollegecgpa.utils.viewLifecycle
import com.facebook.*
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
class SignUpFragment : BaseFragment(R.layout.signup_layout) {
    private var binding: SignupLayoutBinding by viewLifecycle()

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    private val TAG = "google auth"

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    @Inject
    lateinit var basicAuthInterceptor: BasicAuthInterceptor

    private val viewModel: AuthViewModel by viewModels()

    private var signInBtnWasClicked = false

    lateinit var callbackManager: CallbackManager
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
        binding = SignupLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

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

        firebaseAuth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

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
        binding.loginRedirect.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
        }



        binding.signUpBtn.setOnClickListener {
            signInBtnWasClicked = true
            hideKeyboard()
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            val reEnterPassword = binding.editTextReEnterPassword.text.toString()
            val username = binding.editTextUsername.text.toString()

            this.currentEmail = email
            this.currentPassword = password
            this.currentUserName = username

            viewModel.register(email, password, reEnterPassword, username)
        }

        binding.FaceBookSignInBtn.setOnClickListener {
            binding.fbSignupButton.callOnClick()
        }
        binding.GoogleSignInBtn.setOnClickListener {
            //call on click on the main ga button
            binding.gaSignupButton.callOnClick()
            googleSignIn()
        }

        binding.fbSignupButton.apply {
            fragment = this@SignUpFragment
            setReadPermissions("email", "public_profile")
            registerCallback(callbackManager, object : FacebookCallback<LoginResult> {

                override fun onSuccess(result: LoginResult?) {
                    handleFacebookToken(result?.accessToken)
                }

                override fun onCancel() {
                    showSnackBar(
                        "authentication cancelled",
                        null, R.drawable.ic_baseline_whatshot_24,
                        "", Color.BLACK
                    )
                }

                override fun onError(error: FacebookException?) {
                    showSnackBar(
                        "an unknown error occurred",
                        null, R.drawable.ic_baseline_whatshot_24,
                        "", Color.BLACK
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
            val credentials = FacebookAuthProvider.getCredential(it.token)
            firebaseAuth.signInWithCredential(credentials).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("signInWithCredential:success")
                    val user = firebaseAuth.currentUser
                    updateUI(user)
                } else if (!(task.isSuccessful) && task.exception is FirebaseAuthUserCollisionException) {
                    firebaseAuth.currentUser?.linkWithCredential(credentials)
                        ?.addOnSuccessListener { _result ->
                            updateUI(_result.user)
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
        viewModel.registerStatus.observe(viewLifecycleOwner, { result ->
            result?.let {
                when (result.status) {
                    Status.SUCCESS -> {
                        resetUIForLoginButton()
                        showSnackBar(
                            "Successfully registered", null,
                            R.drawable.ic_baseline_emoji_emotions_24,
                            "", Color.GREEN
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
                        redirectLogin()
                    }
                    Status.ERROR -> {
                        resetUIForLoginButton()
                        showSnackBar(
                            result.message ?: "an error occurred", null,
                            R.drawable.ic_baseline_error_outline_24,
                            "", Color.RED
                        )
                    }
                    Status.LOADING -> {
                        updateUIForSignInButton()
                    }
                }
            }
        })

        viewModel.thirdPartyRegisterStatus.observe(viewLifecycleOwner, { result ->
            result?.let {
                when (result.status) {
                    Status.SUCCESS -> {
                        this.isThirdParty = true
                        showSnackBar(
                            "Successfully registered",
                            null, R.drawable.ic_baseline_whatshot_24,
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
                    this.currentUserName = usr.displayName
                }
                viewModel.registerThirdPartyUser(
                    email,
                    usr.displayName ?: "no username"
                )
            }
        }
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
        startActivityForResult(signInIntent, Constants.RC_SIGN_IN)
    }

    private fun updateUIForSignInButton() {
        if (signInBtnWasClicked) {
            binding.signUpBtn.isAllCaps = false
            binding.signUpBtn.text = getString(R.string.loading_wait)
            binding.signUpprogressBar.visibility = View.VISIBLE
        }
    }

    private fun resetUIForLoginButton() {
        binding.signUpBtn.isAllCaps = true
        binding.signUpBtn.text = requireContext().getString(R.string.sign_up)
        binding.signUpprogressBar.visibility = View.GONE
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

    private fun createAnimationsForUIWidgets() {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_left)
        binding.lin.startAnimation(animation)
        val animation1 = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_right)
        binding.signUpBtn.startAnimation(animation1)
        val animation2 = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_slowly)
        binding.signuptxt.startAnimation(animation2)
        val animation3 = AnimationUtils.loadAnimation(requireContext(), R.anim.blink)
        binding.thirdPartyLoginLayoutParent1.startAnimation(animation3)
    }

    override fun onDetach() {
        super.onDetach()
        (activity as MainActivity)
            .window.clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createGoogleSignInRequest()

        val onBackPressed = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_signUpFragment_to_chooseLoginOrSignUpFragment)
            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressed)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //facebook callback
        callbackManager.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent()
        if (requestCode == Constants.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val googleAccount = task.getResult(ApiException::class.java)!!
                Timber.d("firebaseAuthWithGoogle:%s", googleAccount.id)
                firebaseAuthWithGoogle(googleAccount.idToken!!)

            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
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
