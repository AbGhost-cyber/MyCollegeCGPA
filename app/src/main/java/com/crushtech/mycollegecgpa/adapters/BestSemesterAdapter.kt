package com.crushtech.mycollegecgpa.adapters

import android.graphics.Color.RED
import android.graphics.Color.parseColor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.crushtech.mycollegecgpa.R
import com.crushtech.mycollegecgpa.adapters.BestSemesterAdapter.BestSemesterViewHolder
import com.crushtech.mycollegecgpa.data.local.entities.Semester
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.best_semester_item.view.*

class BestSemesterAdapter : Adapter<BestSemesterViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestSemesterViewHolder {
        return BestSemesterViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.best_semester_item, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: BestSemesterViewHolder, position: Int) {
        val semesters = differ.currentList[position]

        holder.itemView.apply {
            Picasso.get().load(R.drawable.best_semester).fit().centerCrop()
                .into(best_semester_image)
            semesterName.text = semesters.semesterName
            if (semesters.courses.isNullOrEmpty()) {
                coursesNames.text = context.getString(R.string.empty_list)
            } else {
                coursesNames.text = semesters.getThreeCoursesName()
            }

            gpa.text = semesters.getGPA().toString()
            if (!semesters.isSynced) {
                ivSynced.setImageResource(R.drawable.ic_cross)
                tvSynced1.text = context.getString(R.string.notSynced)
            } else {
                ivSynced.setImageResource(R.drawable.ic_check)
                tvSynced1.text = context.getString(R.string.isSynced)
            }
            circularProgressBar.apply {
                semesters.courses.forEach {
                    it.gradesPoints.forEach { grade ->
                        val max = grade.APlusGrade
                        progressMax = max
                    }
                }
                progress = semesters.getGPA().toFloat()
                setProgressWithAnimation(progress, 6000)
                backgroundProgressBarWidth = 3f
            }
            if (circularProgressBar.progress < 3) {
                circularProgressBar.progressBarColorEnd = RED
                circularProgressBar.progressBarColorStart = RED
            } else {
                circularProgressBar.progressBarColorEnd = parseColor("#64DD17")
                circularProgressBar.progressBarColorStart = parseColor("#64DD17")
            }
            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(semesters)

                }
            }
        }
    }

    private val diffUtilCallBack = object : DiffUtil.ItemCallback<Semester>() {
        override fun areItemsTheSame(oldItem: Semester, newItem: Semester): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Semester, newItem: Semester): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }

    private var onItemClickListener: ((Semester) -> Unit)? = null

    fun setOnItemClickListener(listener: (Semester) -> Unit) {
        this.onItemClickListener = listener
    }


    val differ = AsyncListDiffer(this, diffUtilCallBack)


    inner class BestSemesterViewHolder(itemView: View) : ViewHolder(itemView)


}