package com.crushtech.myccgpa.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.crushtech.myccgpa.adapters.NotificationItemsAdapter2.NotificationItemViewHolder
import com.crushtech.myccgpa.databinding.NotificationItem2Binding

data class NotificationBottom(
    val itemName: String,
    val itemDes: String,
    val timeDes: String,
    val isChecked: Boolean = false,
    val timeSelected: String
)

class NotificationItemsAdapter2 : RecyclerView.Adapter<NotificationItemViewHolder>() {
    var binding: NotificationItem2Binding? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationItemViewHolder {
        binding = NotificationItem2Binding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NotificationItemViewHolder(binding!!)
    }

    override fun onBindViewHolder(holder: NotificationItemViewHolder, position: Int) {
        val currentItem = differ.currentList[position]
        holder.itemView.apply {
            binding!!.apply {
                notiItemName.text = currentItem.itemName
                notiItemDes.text = currentItem.itemDes
                notiItemChecked.isChecked = currentItem.isChecked
                timeDesTv.text = currentItem.timeDes
                timePickerTv.text = currentItem.timeSelected

                setOnClickListener {
                    onItemClickListener?.let {
                        it(currentItem)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private var onItemClickListener: ((NotificationBottom) -> Unit)? = null

    fun setOnItemClickListener(listener: (NotificationBottom) -> Unit) {
        this.onItemClickListener = listener
    }

    private val diffUtils = object : DiffUtil.ItemCallback<NotificationBottom>() {
        override fun areItemsTheSame(
            oldItem: NotificationBottom,
            newItem: NotificationBottom
        ): Boolean {
            return oldItem.itemName == newItem.itemName
        }

        override fun areContentsTheSame(
            oldItem: NotificationBottom,
            newItem: NotificationBottom
        ): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, diffUtils)

    inner class NotificationItemViewHolder(binding: NotificationItem2Binding) :
        RecyclerView.ViewHolder(binding.root)
}