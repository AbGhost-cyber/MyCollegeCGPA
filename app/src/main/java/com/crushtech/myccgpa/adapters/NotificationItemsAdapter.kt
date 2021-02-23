package com.crushtech.myccgpa.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.crushtech.myccgpa.adapters.NotificationItemsAdapter.NotificationItem1ViewHolder
import com.crushtech.myccgpa.databinding.NotificationItemsBinding

data class NotificationTop(
    val itemName: String,
    val itemDes: String,
    val isChecked: Boolean = false
)

class NotificationItemsAdapter : RecyclerView.Adapter<NotificationItem1ViewHolder>() {
    var binding: NotificationItemsBinding? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationItem1ViewHolder {
        binding = NotificationItemsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NotificationItem1ViewHolder(binding!!)
    }

    override fun onBindViewHolder(holder: NotificationItem1ViewHolder, position: Int) {
        val currentItem = differ.currentList[position]
        holder.itemView.apply {
            binding!!.apply {
                notiItemName.text = currentItem.itemName
                notiItemDes.text = currentItem.itemDes
                notiItemChecked.isChecked = currentItem.isChecked

                setOnClickListener {
                    onItemClickListener?.let { click ->
                        click(currentItem)
                    }
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private var onItemClickListener: ((NotificationTop) -> Unit)? = null

    fun setOnItemClickListener(listener: (NotificationTop) -> Unit) {
        this.onItemClickListener = listener
    }

    private val diffUtils = object : DiffUtil.ItemCallback<NotificationTop>() {
        override fun areItemsTheSame(oldItem: NotificationTop, newItem: NotificationTop): Boolean {
            return oldItem.itemName == newItem.itemName
        }

        override fun areContentsTheSame(
            oldItem: NotificationTop,
            newItem: NotificationTop
        ): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, diffUtils)

    inner class NotificationItem1ViewHolder(binding: NotificationItemsBinding) :
        RecyclerView.ViewHolder(binding.root)
}