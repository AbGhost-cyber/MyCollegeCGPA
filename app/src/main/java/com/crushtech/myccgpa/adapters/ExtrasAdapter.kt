package com.crushtech.myccgpa.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.crushtech.myccgpa.adapters.ExtrasAdapter.ExtraViewHolder
import com.crushtech.myccgpa.databinding.ExtraItemBinding

data class ExtraItems(val itemTitle: String, val itemDrawable: Int)
class ExtrasAdapter : RecyclerView.Adapter<ExtraViewHolder>() {
    var binding: ExtraItemBinding? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExtraViewHolder {
        binding = ExtraItemBinding.inflate(
            LayoutInflater.from(parent.context)
        )
        return ExtraViewHolder(binding!!)
    }

    override fun onBindViewHolder(holder: ExtraViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.itemView.apply {
            binding!!.apply {
                itemTitle.text = item.itemTitle
                itemImage.setImageDrawable(
                    ContextCompat.getDrawable(context, item.itemDrawable)
                )
            }
            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(item)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private val diffUtilCallback = object : DiffUtil.ItemCallback<ExtraItems>() {
        override fun areItemsTheSame(oldItem: ExtraItems, newItem: ExtraItems): Boolean {
            return oldItem.itemTitle == newItem.itemTitle
        }

        override fun areContentsTheSame(oldItem: ExtraItems, newItem: ExtraItems): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, diffUtilCallback)

    private var onItemClickListener: ((ExtraItems) -> Unit)? = null

    fun setOnItemClickListener(listener: (ExtraItems) -> Unit) {
        this.onItemClickListener = listener
    }

    inner class ExtraViewHolder(itemView: ExtraItemBinding) : RecyclerView.ViewHolder(itemView.root)
}