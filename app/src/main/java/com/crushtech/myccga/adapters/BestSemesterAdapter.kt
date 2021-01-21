package com.crushtech.myccga.adapters


import android.graphics.Color.RED
import android.graphics.Color.parseColor
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.crushtech.myccga.R
import com.crushtech.myccga.adapters.BestSemesterAdapter.BestSemesterViewHolder
import com.crushtech.myccga.data.local.entities.Semester
import com.crushtech.myccga.databinding.BestSemesterItemBinding
import com.crushtech.myccga.utils.Constants.getHighestGrade
import com.squareup.picasso.Picasso

class BestSemesterAdapter : Adapter<BestSemesterViewHolder>() {
    lateinit var binding: BestSemesterItemBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestSemesterViewHolder {
        binding = BestSemesterItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return BestSemesterViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: BestSemesterViewHolder, position: Int) {
        val semesters = differ.currentList[position]

        holder.itemView.apply {
            Picasso.get().load(R.drawable.best_semester).fit().centerCrop()
                .into(binding.bestSemesterImage)

            val anim = AnimationUtils.loadAnimation(context, R.anim.left_to_right)
            binding.shineView.startAnimation(anim)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    binding.shineView.startAnimation(anim)
                }

                override fun onAnimationRepeat(animation: Animation?) {
                }

            })



            binding.semesterName.text = semesters.semesterName
            if (semesters.courses.isNullOrEmpty()) {
                binding.coursesNames.text = context.getString(R.string.empty_list)
            } else {
                binding.coursesNames.text = semesters.getThreeCoursesName()
            }

            binding.gpa.text = semesters.getGPA().toString()
            if (!semesters.isSynced) {
                binding.ivSynced.setImageResource(R.drawable.ic_cross)
                binding.tvSynced1.text = context.getString(R.string.notSynced)
            } else {
                binding.ivSynced.setImageResource(R.drawable.ic_check)
                binding.tvSynced1.text = context.getString(R.string.isSynced)
            }
            binding.circularProgressBar.apply {
                semesters.courses.forEach {
                    if (it.gradesPoints.isNotEmpty()) {
                        val grade = it.gradesPoints[0]
                        val max = getHighestGrade(grade)
                        max?.let {
                            progressMax = max
                        }
                    }
                }
                progress = semesters.getGPA().toFloat()
                setProgressWithAnimation(progress, 6000)
                backgroundProgressBarWidth = 3f
            }
            if (binding.circularProgressBar.progress < 3) {
                binding.circularProgressBar.progressBarColorEnd = RED
                binding.circularProgressBar.progressBarColorStart = RED
            } else {
                binding.circularProgressBar.progressBarColorEnd = parseColor("#64DD17")
                binding.circularProgressBar.progressBarColorStart = parseColor("#64DD17")
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

    val differ = AsyncListDiffer(this, diffUtilCallBack)


    inner class BestSemesterViewHolder(binding: BestSemesterItemBinding) : ViewHolder(binding.root)


}