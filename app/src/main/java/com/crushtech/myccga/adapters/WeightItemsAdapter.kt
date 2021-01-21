package com.crushtech.myccga.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.crushtech.myccga.adapters.WeightItemsAdapter.WeightViewHolder
import com.crushtech.myccga.data.local.entities.GradeSimplified
import com.crushtech.myccga.databinding.WeightItemsBinding

class WeightItemsAdapter : RecyclerView.Adapter<WeightViewHolder>() {
    var binding: WeightItemsBinding? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightViewHolder {

        binding = WeightItemsBinding.inflate(
            LayoutInflater.from(parent.context), parent,
            false
        )
        return WeightViewHolder(binding!!)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: WeightViewHolder, position: Int) {
        val grade = differ.currentList[position]
        holder.itemView.apply {
            binding!!.tvGrade.text = grade.name
            binding!!.tvGradePoint.text = grade.gradePoint.toString()

            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(grade)
                }
            }
        }
    }

    private val differUtils = object : DiffUtil.ItemCallback<GradeSimplified>() {
        override fun areItemsTheSame(oldItem: GradeSimplified, newItem: GradeSimplified): Boolean {
            return oldItem.name == oldItem.name
        }

        override fun areContentsTheSame(
            oldItem: GradeSimplified,
            newItem: GradeSimplified
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }

    val differ = AsyncListDiffer(this, differUtils)

    private var onItemClickListener: ((GradeSimplified) -> Unit)? = null

    fun setOnItemClickListener(listener: (GradeSimplified) -> Unit) {
        this.onItemClickListener = listener
    }

    inner class WeightViewHolder(itemView: WeightItemsBinding) :
        RecyclerView.ViewHolder(itemView.root)
}