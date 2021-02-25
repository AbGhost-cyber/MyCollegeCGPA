package com.crushtech.myccgpa.ui.fragments.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crushtech.myccgpa.MainActivity
import com.crushtech.myccgpa.R
import com.crushtech.myccgpa.adapters.ExtraItems
import com.crushtech.myccgpa.adapters.Group
import com.crushtech.myccgpa.adapters.GroupAdapter
import com.crushtech.myccgpa.databinding.SettingsLayoutBinding
import com.crushtech.myccgpa.dialogs.LogoutDialogFragment
import com.crushtech.myccgpa.notification.OneSignalHelper
import com.crushtech.myccgpa.ui.BaseFragment
import com.crushtech.myccgpa.utils.Constants
import com.crushtech.myccgpa.utils.Constants.PRIVACY_POLICY
import com.crushtech.myccgpa.utils.viewLifecycle
import com.facebook.login.LoginManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val LOG_OUT_DIALOG = "log out dialog"


@AndroidEntryPoint
class SettingsFragment : BaseFragment(R.layout.settings_layout) {
    private var binding: SettingsLayoutBinding by viewLifecycle()
    private var groups: ArrayList<Group>? = null
    private var accountItemsList: ArrayList<ExtraItems>? = null
    private var supportItemsList: ArrayList<ExtraItems>? = null
    private var legalItemsList: ArrayList<ExtraItems>? = null
    private var logoutItemList: ArrayList<ExtraItems>? = null
    private var groupAdapter: GroupAdapter? = null

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private var isThirdPartyUser = false

    private val settingsViewModel: SettingsViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SettingsLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).apply {
            showAppBar()
            showMainActivityUI()
            activityMainBinding.titleBarText.text = getString(R.string.settings)
        }
        isThirdPartyUser = sharedPreferences.getBoolean(
            Constants.IS_THIRD_PARTY,
            Constants.NOT_THIRD_PARTY
        )

        val manager = requireContext().packageManager
        val info = manager.getPackageInfo(
            requireContext().packageName,
            PackageManager.GET_ACTIVITIES
        )
        binding.appVersionTv.text = info.versionName

        if (savedInstanceState != null) {
            val logOutDialog = parentFragmentManager.findFragmentByTag(LOG_OUT_DIALOG)
                    as LogoutDialogFragment?
            logOutDialog?.setPositiveListener { clicked ->
                if (clicked) {
                    settingsViewModel.logOutCurrentUser(this)
                }
                val user = Firebase.auth.currentUser
                if (user?.providerId == "google.com") {
                    user.delete()
                }
                LoginManager.getInstance().logOut()
            }
        }
        setUpRecyclerView()
        if (groupAdapter != null) {
            groupAdapter!!.accountItemAdapter.setOnItemClickListener { position ->
                when (position) {
                    0 -> {
                        findNavController().navigate(
                            SettingsFragmentDirections
                                .actionExtrasFragmentToPersonalInfoFragment()
                        )
                    }
                    1 -> {
                        findNavController().navigate(
                            SettingsFragmentDirections
                                .actionExtrasFragmentToNotificationFragment()
                        )
                    }
                    2 -> findNavController().navigate(
                        SettingsFragmentDirections
                            .actionOthersFragmentToWeightFragment()
                    )
                    3 -> findNavController().navigate(
                        SettingsFragmentDirections
                            .actionExtrasFragmentToAboutAppFragment()
                    )
                    4 -> findNavController().navigate(
                        SettingsFragmentDirections
                            .actionExtrasFragmentToSemesterRequestFragment()
                    )
                }
            }
            groupAdapter!!.supportItemAdapter.setOnItemClickListener { position ->
                when (position) {
                    0 -> rateAppFunction()
                    1 -> shareAppFunction()
                    2 -> showSnackBar(
                        "coming soon...", null,
                        R.drawable.ic_baseline_bubble_chart_24,
                        "", Color.BLACK
                    )
                }
            }
            groupAdapter!!.legalItemAdapter.setOnItemClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(PRIVACY_POLICY)
                startActivity(intent)
            }
            groupAdapter!!.logoutItemAdapter.setOnItemClickListener { position ->
                when (position) {
                    0 -> showLogOutDialog()
                }
            }
        }
    }

    private fun initGroupData() {
        groups = ArrayList()
        groups!!.apply {
            add(Group("ACCOUNT"))
            add(Group("SUPPORT"))
            add(Group("LEGAL"))
            //for log out, no group title needs to be specified
            add(Group(""))
        }
    }

    private fun setUpRecyclerView() {
        initGroupData()
        setUpGroupItemsData()
        groupAdapter = GroupAdapter()
        groupAdapter!!.differ.submitList(groups!!)
        binding.rvExtrasLayout.apply {
            adapter = groupAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        groupAdapter!!.apply {
            accountItemAdapter.differ.submitList(accountItemsList!!)
            legalItemAdapter.differ.submitList(legalItemsList!!)
            supportItemAdapter.differ.submitList(supportItemsList!!)
            logoutItemAdapter.differ.submitList(logoutItemList!!)
        }
    }


    private fun setUpGroupItemsData() {
        accountItemsList = ArrayList()
        accountItemsList!!.apply {
            add(
                ExtraItems(
                    "Personal Information",
                    R.drawable.ic_baseline_chevron_right_24
                )
            )
            add(
                ExtraItems(
                    "Notifications",
                    R.drawable.ic_baseline_chevron_right_24
                )
            )
            add(
                ExtraItems(
                    "Edit Course Grade Point",
                    R.drawable.ic_baseline_chevron_right_24
                )
            )
            add(
                ExtraItems(
                    "About My College CGPA and Tips",
                    R.drawable.ic_baseline_chevron_right_24
                )
            )
            add(
                ExtraItems(
                    "View All Semester Requests",
                    R.drawable.ic_baseline_chevron_right_24
                )
            )
        }
        supportItemsList = ArrayList()
        supportItemsList!!.apply {
            add(
                ExtraItems(
                    "Rate App",
                    R.drawable.ic_outline_rate_review_24
                )
            )
            add(
                ExtraItems(
                    "Invite Friends",
                    R.drawable.ic_outline_people_24
                )
            )
            add(
                ExtraItems(
                    "Contact Us",
                    R.drawable.contact_us_ic
                )
            )
        }
        legalItemsList = ArrayList()
        legalItemsList!!.apply {
            add(
                ExtraItems(
                    "Terms of Service",
                    R.drawable.term_of_service_ic
                )
            )
            add(
                ExtraItems(
                    "Privacy",
                    R.drawable.privacy_ic
                )
            )
        }
        logoutItemList = ArrayList()
        logoutItemList!!.add(
            ExtraItems(
                "Log Out",
                null
            )
        )
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
                    settingsViewModel.logOutCurrentUser(this)
                    val user = Firebase.auth.currentUser
                    if (user?.providerId == "google.com") {
                        user.delete()
                    }
                    LoginManager.getInstance().logOut()
                    OneSignalHelper.removeUserExternalId()
                }
            }
        }.show(parentFragmentManager, LOG_OUT_DIALOG)
    }

    override fun onDestroy() {
        groupAdapter?.let {
            it.apply {
                binding = null
                accountItemAdapter.binding = null
                legalItemAdapter.binding = null
                supportItemAdapter.binding = null
                logoutItemAdapter.binding = null
            }
        }
        super.onDestroy()
    }
}