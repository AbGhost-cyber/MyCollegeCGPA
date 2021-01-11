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
import com.crushtech.mycollegecgpa.databinding.CourseItemsBinding
import com.crushtech.mycollegecgpa.dialogs.AddCourseDialogFragment
import com.crushtech.mycollegecgpa.ui.fragments.course.ADD_COURSE_DIALOG
import com.crushtech.mycollegecgpa.ui.fragments.course.CourseListFragment

class CourseAdapter(private val courseListFragment: CourseListFragment) :
    RecyclerView.Adapter<CourseViewHolder>() {

    lateinit var binding: CourseItemsBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        binding = CourseItemsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return CourseViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }


    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.itemView.apply {

            val courses = differ.currentList[position]
            binding.tvCourseName.text = courses.courseName


            val creditHours = if (courses.creditHours <= 1) {
                "${courses.creditHours} credit hour"
            } else {
                "${courses.creditHours} credit hours"
            }

            binding.tvCreditHours.text = creditHours

            if (courses.grade.equals("F", true) ||
                courses.grade.equals("D+", true) ||
                courses.grade.equals("D", true)
            ) {

                binding.tvGrade.setTextColor(Color.RED)
            } else {
                binding.tvGrade.setTextColor(Color.parseColor("#4A56E2"))
            }
            binding.tvGrade.text = courses.grade

            binding.othersIv.setOnClickListener {
                binding.actionsLayout.visibility = View.VISIBLE
                binding.actionsLayout.layoutParams.height = binding.itemsLayout.height / 2
                val margins = binding.actionsLayout.layoutParams as ViewGroup.MarginLayoutParams
                margins.setMargins(8, 8, 8, 8)
                ObjectAnimator.ofFloat(
                    binding.itemsLayout, "translationX",
                    -400F
                ).apply {
                    duration = 50
                    start()
                }
                enableViews(listOf(binding.editCourse, binding.deleteCourse, binding.closeView))
            }
            binding.closeView.setOnClickListener {
                binding.actionsLayout.layoutParams.height =
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                val margins = binding.actionsLayout.layoutParams as ViewGroup.MarginLayoutParams
                margins.setMargins(8, 8, 8, 8)
                ObjectAnimator.ofFloat(
                    binding.itemsLayout,
                    "translationX",
                    0F
                ).apply {
                    duration = 50
                    start()
                }
                disableViews(listOf(binding.editCourse, binding.deleteCourse, binding.closeView))
            }
            binding.editCourse.setOnClickListener {
                if (courseListFragment.isOwner) {
                    val bundle = Bundle()
                    bundle.putSerializable("courses", courses)
                    AddCourseDialogFragment().apply {
                        arguments = bundle
                        setPositiveListener {
                            courseListFragment.updateCourse(it, "course updated", position)
                            ObjectAnimator.ofFloat(
                                binding.itemsLayout, "translationX",
                                0F
                            ).apply {
                                duration = 50
                                start()
                            }
                            disableViews(
                                listOf(
                                    binding.editCourse,
                                    binding.deleteCourse,
                                    binding.closeView
                                )
                            )
                        }
                    }.show(courseListFragment.parentFragmentManager, ADD_COURSE_DIALOG)
                }
            }

            binding.deleteCourse.setOnClickListener {
                if (courseListFragment.isOwner) {
                    courseListFragment.showDeleteCourseDialog(courses)
                    ObjectAnimator.ofFloat(
                        binding.itemsLayout, "translationX",
                        0F
                    ).apply {
                        duration = 50
                        start()
                    }
                    disableViews(
                        listOf(
                            binding.editCourse,
                            binding.deleteCourse,
                            binding.closeView
                        )
                    )
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
                binding.viewCourseColor.background = wrappedDrawable
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

    private fun disableViews(views: List<View>) {
        views.forEach {
            it.isEnabled = false
        }

    }

    private fun enableViews(views: List<View>) {
        views.forEach {
            it.isEnabled = true
        }

    }

    val differ = AsyncListDiffer(this, diffUtilCallBack)

    inner class CourseViewHolder(binding: CourseItemsBinding) :
        RecyclerView.ViewHolder(binding.root)

}



