package com.crushtech.mycollegecgpa.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.crushtech.mycollegecgpa.R
import com.crushtech.mycollegecgpa.adapters.CourseAdapter.CourseViewHolder
import com.crushtech.mycollegecgpa.data.local.entities.Courses
import kotlinx.android.synthetic.main.course_items.view.*

class CourseAdapter : RecyclerView.Adapter<CourseViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        return CourseViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.course_items, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val courses = differ.currentList[position]
        holder.itemView.apply {
            tvCourseName.text = courses.courseName

            val creditHours = if (courses.creditHours <= 1) {
                "${courses.creditHours} credit hour"
            } else {
                "${courses.creditHours} credit hours"
            }

            tvCreditHours.text = creditHours

            if (courses.grade.equals("F", true) ||
                courses.grade.equals("D-", true)
            ) {

                tvGrade.setTextColor(Color.RED)
            } else {
                tvGrade.setTextColor(Color.parseColor("#4A56E2"))
            }
            tvGrade.text = courses.grade


            val drawable = ResourcesCompat.getDrawable(
                resources,
                R.drawable.thin_shape, null
            )
            drawable?.let {
                val wrappedDrawable = DrawableCompat.wrap(it)
                val color = Color.parseColor("#${courses.color}")
                DrawableCompat.setTint(wrappedDrawable, color)
                viewCourseColor.background = wrappedDrawable
            }
            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(courses)
                }
            }
        }
    }

    private val diffUtilCallBack = object : DiffUtil.ItemCallback<Courses>() {
        override fun areItemsTheSame(oldItem: Courses, newItem: Courses): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Courses, newItem: Courses): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }

    private var onItemClickListener: ((Courses) -> Unit)? = null

    fun setOnItemClickListener(listener: (Courses) -> Unit) {
        this.onItemClickListener = listener
    }

    val differ = AsyncListDiffer(this, diffUtilCallBack)

    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

