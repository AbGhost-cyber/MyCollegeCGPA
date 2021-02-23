package com.crushtech.myccgpa.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.crushtech.myccgpa.R
import com.crushtech.myccgpa.adapters.SemesterRequestItemsAdapter.RequestItemViewHolder
import com.crushtech.myccgpa.data.local.entities.STATE
import com.crushtech.myccgpa.data.local.entities.SemesterRequests
import com.crushtech.myccgpa.databinding.SemesterRequestItemsBinding

class SemesterRequestItemsAdapter : RecyclerView.Adapter<RequestItemViewHolder>() {
    var binding: SemesterRequestItemsBinding? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestItemViewHolder {
        binding = SemesterRequestItemsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return RequestItemViewHolder(binding!!)
    }

    override fun onBindViewHolder(holder: RequestItemViewHolder, position: Int) {
        val semesterRequest = differ.currentList[position]
        holder.itemView.apply {
            binding!!.apply {
                val sharedByString = "Shared by: ${semesterRequest.owner}"
                semSharedBy.text = sharedByString
                val semesterIdString = "Semester Id: ${semesterRequest.semesterId}"
                sharedSemesterId.text = semesterIdString
                val color = when (semesterRequest.state) {
                    STATE.ACCEPTED -> R.color.progress_color
                    STATE.REJECTED -> android.R.color.holo_red_light
                    STATE.PENDING -> R.color.yellow
                }
                sharedSemesterStatus.apply {
                    setTextColor(ContextCompat.getColor(context, color))
                    text = semesterRequest.state.name
                }
                setOnClickListener {
                    onItemClickListener?.let { click ->
                        click(semesterRequest)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private val diffCallback = object : DiffUtil.ItemCallback<SemesterRequests>() {
        override fun areItemsTheSame(
            oldItem: SemesterRequests,
            newItem: SemesterRequests
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: SemesterRequests,
            newItem: SemesterRequests
        ): Boolean {
            return oldItem == newItem
        }

    }
    private var onItemClickListener: ((SemesterRequests) -> Unit)? = null

    fun setOnItemClickListener(listener: (SemesterRequests) -> Unit) {
        this.onItemClickListener = listener
    }

    val differ = AsyncListDiffer(this, diffCallback)

    inner class RequestItemViewHolder(itemView: SemesterRequestItemsBinding) :
        RecyclerView.ViewHolder(itemView.root)
}