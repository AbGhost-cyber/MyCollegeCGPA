package com.crushtech.mycollegecgpa.adapters

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.crushtech.mycollegecgpa.R
import com.crushtech.mycollegecgpa.adapters.CourseAdapter.CourseViewHolder
import com.crushtech.mycollegecgpa.data.local.entities.Courses
import com.crushtech.mycollegecgpa.dialogs.AddCourseDialogFragment
import com.crushtech.mycollegecgpa.ui.fragments.course.ADD_COURSE_DIALOG
import com.crushtech.mycollegecgpa.ui.fragments.course.CourseListFragment
import kotlinx.android.synthetic.main.course_items.view.*

class CourseAdapter(private val courseListFragment: CourseListFragment) :
    RecyclerView.Adapter<CourseViewHolder>() {

    var itemView: View? = null
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
        holder.itemView.apply {
            itemView = this

            val courses = differ.currentList[position]
            tvCourseName.text = courses.courseName


            val creditHours = if (courses.creditHours <= 1) {
                "${courses.creditHours} credit hour"
            } else {
                "${courses.creditHours} credit hours"
            }

            tvCreditHours.text = creditHours

            if (courses.grade.equals("F", true) ||
                courses.grade.equals("D+", true) ||
                courses.grade.equals("D", true)
            ) {

                tvGrade.setTextColor(Color.RED)
            } else {
                tvGrade.setTextColor(Color.parseColor("#4A56E2"))
            }
            tvGrade.text = courses.grade

            others_iv.setOnClickListener {
                actionsLayout.visibility = View.VISIBLE
                actionsLayout.layoutParams.height = itemsLayout.height / 2
                val margins = actionsLayout.layoutParams as ViewGroup.MarginLayoutParams
                margins.setMargins(8, 8, 8, 8)
                ObjectAnimator.ofFloat(
                    itemsLayout, "translationX",
                    -400F
                ).apply {
                    duration = 50
                    start()
                }
            }
            close_view.setOnClickListener {
                actionsLayout.layoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
                val margins = actionsLayout.layoutParams as ViewGroup.MarginLayoutParams
                margins.setMargins(8, 8, 8, 8)
                ObjectAnimator.ofFloat(itemsLayout, "translationX", 0F).apply {
                    duration = 50
                    start()
                }
            }
            edit_course.setOnClickListener {
                val bundle = Bundle()
                bundle.putSerializable("courses", courses)
                AddCourseDialogFragment().apply {
                    arguments = bundle
                    setPositiveListener {
                        courseListFragment.updateCourse(it, "course updated", position)
                        ObjectAnimator.ofFloat(
                            itemsLayout, "translationX",
                            0F
                        ).apply {
                            duration = 50
                            start()
                        }
                    }
                }.show(courseListFragment.parentFragmentManager, ADD_COURSE_DIALOG)
            }

            delete_course.setOnClickListener {
                courseListFragment.showDeleteCourseDialog(courses)
                ObjectAnimator.ofFloat(
                    itemsLayout, "translationX",
                    0F
                ).apply {
                    duration = 50
                    start()
                }
            }

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
                    click(courses, position)
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

    private var onItemClickListener: ((Courses, Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Courses, Int) -> Unit) {
        this.onItemClickListener = listener
    }

    val differ = AsyncListDiffer(this, diffUtilCallBack)

    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}

