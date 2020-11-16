package com.crushtech.mycollegecgpa.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.crushtech.mycollegecgpa.R
import com.crushtech.mycollegecgpa.adapters.WeightItemsAdapter.WeightViewHolder
import com.crushtech.mycollegecgpa.data.local.entities.GradeSimplified
import kotlinx.android.synthetic.main.weight_items.view.*

class WeightItemsAdapter : RecyclerView.Adapter<WeightViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightViewHolder {
        return WeightViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.weight_items, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: WeightViewHolder, position: Int) {
        val grade = differ.currentList[position]
        holder.itemView.apply {
            tvGrade.text = grade.name
            tvGradePoint.text = grade.gradePoint.toString()

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

    inner class WeightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}