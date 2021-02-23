package com.crushtech.myccgpa.ui.fragments.personalinfo

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.crushtech.myccgpa.MainActivity
import com.crushtech.myccgpa.R
import com.crushtech.myccgpa.databinding.PersonalInfoLayoutBinding
import com.crushtech.myccgpa.ui.BaseFragment
import com.crushtech.myccgpa.utils.Constants.KEY_LOGGED_IN_EMAIL
import com.crushtech.myccgpa.utils.Constants.KEY_USERNAME
import com.crushtech.myccgpa.utils.Constants.NO_EMAIL
import com.crushtech.myccgpa.utils.Constants.NO_USERNAME
import com.crushtech.myccgpa.utils.viewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PersonalInfoFragment : BaseFragment(R.layout.personal_info_layout) {
    @Inject
    lateinit var sharedPrefs: SharedPreferences

    private var binding: PersonalInfoLayoutBinding by viewLifecycle()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PersonalInfoLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as MainActivity).apply {
            hideMainActivityUI()
            showAppBar()
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_chevron_left_24)
            activityMainBinding.titleBarText.text = getString(R.string.personal_info)
        }
        setUpUserProfile()
        //  super.onViewCreated(view, savedInstanceState)
    }

    private fun setUpUserProfile() {
        val currentUserEmail = sharedPrefs.getString(
            KEY_LOGGED_IN_EMAIL, NO_EMAIL
        ) ?: NO_EMAIL

        val currentUsername = sharedPrefs.getString(
            KEY_USERNAME, NO_USERNAME
        ) ?: NO_USERNAME

        val userFirstName = currentUsername.split(" ").getOrNull(0)
        val userLastName = currentUsername.split(" ").getOrNull(1)

        binding.apply {
            FirstName.text = userFirstName ?: "None"
            LastName.text = userLastName ?: "none"
            Email.text = if (currentUserEmail == NO_EMAIL) "None" else currentUserEmail
            UserName.text = if (currentUsername == NO_USERNAME) "None" else currentUsername
            textToImage.text = if (currentUsername == NO_USERNAME) "None"
            else currentUsername[0].toUpperCase().toString()

            dummyHolder.setOnClickListener {
                showSnackBar(
                    "Coming Soon....", null,
                    R.drawable.ic_baseline_error_outline_24,
                    "", Color.BLACK
                )
            }
        }

    }
}