package com.crushtech.myccgpa.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.crushtech.myccgpa.MainActivity
import com.crushtech.myccgpa.R
import com.crushtech.myccgpa.adapters.NotificationBottom
import com.crushtech.myccgpa.adapters.NotificationGroupAdapter
import com.crushtech.myccgpa.adapters.NotificationTop
import com.crushtech.myccgpa.databinding.NotificationLayoutBinding
import com.crushtech.myccgpa.ui.BaseFragment
import com.crushtech.myccgpa.utils.viewLifecycle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationFragment : BaseFragment(R.layout.notification_layout) {
    private var binding: NotificationLayoutBinding by viewLifecycle()
    private var notTopList: ArrayList<NotificationTop>? = null
    private var notBotList: ArrayList<NotificationBottom>? = null
    private lateinit var groupNotAdapter: NotificationGroupAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NotificationLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as MainActivity).apply {
            showAppBar()
            showMainActivityUI()
            activityMainBinding.titleBarText.text = getString(R.string.notifications)
        }
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        //initGroupData()
        setUpGroupItemsData()
        groupNotAdapter = NotificationGroupAdapter()
        binding.apply {
            notificationRv.apply {
                adapter = groupNotAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
        }
        groupNotAdapter.apply {
            notificationTopAdapter.differ.submitList(notTopList!!)
            notificationBottomAdapter.differ.submitList(notBotList!!)
        }

    }


    private fun setUpGroupItemsData() {
        notBotList = ArrayList()
        notBotList!!.apply {
            add(
                NotificationBottom(
                    itemName = "Great motivational quotes, curated for you",
                    itemDes = "Daily and weekly motivational quotes curated for you to keep you ahead of your college performance.",
                    timeDes = "What time would you like your daily student motivation quotes delivered",
                    timeSelected = "10 AM"
                )
            )
        }
        notTopList = ArrayList()
        notTopList!!.apply {
            add(
                NotificationTop(
                    itemName = "From your friends or family",
                    itemDes = "Get notified when a semester request is being shared  to you or when you share one"
                )
            )
            add(
                NotificationTop(
                    itemName = "Celebrating little wins",
                    itemDes = "Looking back at what progress you've reached recently in the app"
                )
            )
            add(
                NotificationTop(
                    itemName = "More to be added soon....",
                    itemDes = "coming soon....."
                )
            )
        }
    }
}