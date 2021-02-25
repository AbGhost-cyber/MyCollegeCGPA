package com.crushtech.myccgpa.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crushtech.myccgpa.R
import com.crushtech.myccgpa.adapters.NotificationGroupAdapter.NotificationGroupVh
import com.crushtech.myccgpa.databinding.NotificationGroupItemsBinding

class NotificationGroupAdapter : RecyclerView.Adapter<NotificationGroupVh>() {
    var binding: NotificationGroupItemsBinding? = null
    var notificationBottomAdapter = NotificationItemsAdapter2()
    var notificationTopAdapter = NotificationItemsAdapter()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationGroupVh {
        binding = NotificationGroupItemsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NotificationGroupVh(binding!!)
    }

    override fun onBindViewHolder(holder: NotificationGroupVh, position: Int) {
        holder.itemView.apply {
            binding!!.apply {
                setList(groupRecyclerView, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return 2
    }

    private fun setList(recyclerView: RecyclerView, position: Int) {
        when (position) {
            0 -> setNotificationTopList(recyclerView)
            1 -> setNotificationBottomList(recyclerView)
        }
    }

    private fun setNotificationBottomList(recyclerView: RecyclerView) {
        recyclerView.apply {
            adapter = notificationBottomAdapter
            layoutManager = LinearLayoutManager(context)
            isNestedScrollingEnabled = true
            isSaveEnabled = true
            addItemDecoration(
                DividerItemDecoration(
                    recyclerView.context,
                    DividerItemDecoration.VERTICAL
                ).also {
                    val divider = ContextCompat.getDrawable(
                        recyclerView.context, R.drawable.extra_items_divider
                    )
                    divider?.let { drawable ->
                        it.setDrawable(drawable)
                    }
                }
            )
        }
    }

    private fun setNotificationTopList(recyclerView: RecyclerView) {
        recyclerView.apply {
            adapter = notificationTopAdapter
            layoutManager = LinearLayoutManager(context)
            isNestedScrollingEnabled = true
            isSaveEnabled = true
            addItemDecoration(
                DividerItemDecoration(
                    recyclerView.context,
                    DividerItemDecoration.VERTICAL
                ).also {
                    val divider = ContextCompat.getDrawable(
                        recyclerView.context, R.drawable.extra_items_divider
                    )
                    divider?.let { drawable ->
                        it.setDrawable(drawable)
                    }
                }
            )
        }
    }

    inner class NotificationGroupVh(binding: NotificationGroupItemsBinding) :
        RecyclerView.ViewHolder(binding.root)
}