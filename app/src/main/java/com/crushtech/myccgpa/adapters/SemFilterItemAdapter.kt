package com.crushtech.myccgpa.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.crushtech.myccgpa.adapters.SemFilterItemAdapter.SemFilterItemViewHolder
import com.crushtech.myccgpa.databinding.SemesterFilterItemsBinding
import com.google.android.material.switchmaterial.SwitchMaterial

data class FilterItems(val filterItemText: String, var isChecked: Boolean = false)
class SemFilterItemAdapter : RecyclerView.Adapter<SemFilterItemViewHolder>() {
    var binding: SemesterFilterItemsBinding? = null
    private var lastCheckedSwitch: SwitchMaterial? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SemFilterItemViewHolder {
        binding = SemesterFilterItemsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return SemFilterItemViewHolder(binding!!)
    }

    override fun onBindViewHolder(holder: SemFilterItemViewHolder, position: Int) {
        val currentItem = differ.currentList[position]
        holder.itemView.apply {
            binding!!.apply {
                filterIsChecked.isChecked = currentItem.isChecked
                filterItemName.text = currentItem.filterItemText

                filterIsChecked.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        if (lastCheckedSwitch != null && lastCheckedSwitch != buttonView) {
                            lastCheckedSwitch!!.isChecked = false
                        }
                        lastCheckedSwitch = filterIsChecked
                        onItemClickListener?.let { click ->
                            click(position)
                        }
                    }
                }

            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private val diffUtilCallback = object : DiffUtil.ItemCallback<FilterItems>() {
        override fun areItemsTheSame(oldItem: FilterItems, newItem: FilterItems): Boolean {
            return oldItem.filterItemText == newItem.filterItemText
        }

        override fun areContentsTheSame(oldItem: FilterItems, newItem: FilterItems): Boolean {
            return oldItem == newItem
        }

    }
    private var onItemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        this.onItemClickListener = listener
    }

    val differ = AsyncListDiffer(this, diffUtilCallback)

    inner class SemFilterItemViewHolder(binding: SemesterFilterItemsBinding) :
        RecyclerView.ViewHolder(binding.root)
}