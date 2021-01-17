package com.crushtech.mycollegecgpa.ui.fragments.extras

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.crushtech.mycollegecgpa.MainActivity
import com.crushtech.mycollegecgpa.R
import com.crushtech.mycollegecgpa.databinding.ExtrasLayoutBinding
import com.crushtech.mycollegecgpa.dialogs.LogoutDialogFragment
import com.crushtech.mycollegecgpa.ui.BaseFragment
import com.crushtech.mycollegecgpa.utils.Constants
import com.crushtech.mycollegecgpa.utils.Constants.PRIVACY_POLICY
import com.crushtech.mycollegecgpa.utils.viewLifecycle
import com.facebook.login.LoginManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val LOG_OUT_DIALOG = "log out dialog"


@AndroidEntryPoint
class OthersFragment : BaseFragment(R.layout.extras_layout) {
    private var binding: ExtrasLayoutBinding by viewLifecycle()

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private var isThirdPartyUser = false

    private val extrasViewModel: ExtrasViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ExtrasLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).apply {
            showAppBar()
            showMainActivityUI()
            activityMainBinding.titleBarText.text = getString(R.string.my_extras)
        }
        isThirdPartyUser = sharedPreferences.getBoolean(
            Constants.IS_THIRD_PARTY,
            Constants.NOT_THIRD_PARTY
        )


        setClickAnimationForTexts(
            listOf(
                binding.editCourseGp,
                binding.ShareApp,
                binding.AboutApp,
                binding.rateApp,
                binding.privacyPolicy,
                binding.logOut
            )
        )

        if (savedInstanceState != null) {
            val logOutDialog = parentFragmentManager.findFragmentByTag(LOG_OUT_DIALOG)
                    as LogoutDialogFragment?
            logOutDialog?.setPositiveListener { clicked ->
                if (clicked) {
                    extrasViewModel.logOutCurrentUser(this)
                }
                val user = Firebase.auth.currentUser
                if (user?.providerId == "google.com") {
                    user.delete()
                }
                LoginManager.getInstance().logOut()
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
        val shareBody = getString(R.string.share_info) + " " + strAppLink
        val shareSub = "APP NAME/TITLE"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub)
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(shareIntent, "Share Using"))
    }

    private fun showLogOutDialog() {
        LogoutDialogFragment().apply {
            setPositiveListener { clicked ->
                if (clicked) {
                    extrasViewModel.logOutCurrentUser(this)
                    val user = Firebase.auth.currentUser
                    if (user?.providerId == "google.com") {
                        user.delete()
                    }
                    LoginManager.getInstance().logOut()
                }
            }
        }.show(parentFragmentManager, LOG_OUT_DIALOG)
    }


    private fun setClickAnimationForTexts(textLists: List<TextView>) {
        val animation = AnimationUtils.loadAnimation(
            requireContext(), android.R.anim.slide_in_left
        )
        textLists.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                textView.startAnimation(animation)
                when (index) {
                    0 -> {
                        findNavController().navigate(
                            R.id.action_othersFragment_to_weightFragment
                        )
                    }
                    1 -> shareAppFunction()
                    2 -> findNavController().navigate(
                        R.id.action_extrasFragment_to_aboutAppFragment
                    )
                    3 -> rateAppFunction()
                    4 -> {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(PRIVACY_POLICY)
                        startActivity(intent)
                    }
                    5 -> showLogOutDialog()
                }
            }
        }
    }
}