package com.crushtech.myccgpa.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.crushtech.myccgpa.R
import com.crushtech.myccgpa.adapters.ExtrasAdapter.ExtraViewHolder
import com.crushtech.myccgpa.databinding.ExtraItemBinding

data class ExtraItems(val itemTitle: String, val itemDrawable: Int?)
class ExtrasAdapter : RecyclerView.Adapter<ExtraViewHolder>() {
    var binding: ExtraItemBinding? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExtraViewHolder {
        binding = ExtraItemBinding.inflate(
            LayoutInflater.from(parent.context)
        )
        return ExtraViewHolder(binding!!)
    }

    override fun onBindViewHolder(holder: ExtraViewHolder, position: Int) {
        val extraItems = differ.currentList[position]
        holder.itemView.apply {
            binding?.let { bind ->
                bind.itemTitle.text = extraItems.itemTitle
                if (bind.itemTitle.text == "Log Out") {
                    bind.itemTitle.setTextColor(
                        ContextCompat.getColor(
                            context,
                            android.R.color.holo_red_light
                        )
                    )
                } else {
                    bind.itemTitle.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.colorPrimary
                        )
                    )
                }
                bind.itemImage.setImageDrawable(
                    extraItems.itemDrawable?.let {
                        ContextCompat.getDrawable(context, it)
                    }

                )
                setOnClickListener {
                    onItemClickListener?.invoke(position)
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

    private var onItemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        this.onItemClickListener = listener
    }

    inner class ExtraViewHolder(
        binding: ExtraItemBinding
    ) : RecyclerView.ViewHolder(binding.root)
}